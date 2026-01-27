package com.kbs.backend.websocket.voice;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

import com.kbs.backend.stt.SttService;

@Log4j2
@RequiredArgsConstructor
public class VoiceAudioWorker implements Runnable {

    private final VoiceSession session;
    private final SttService sttService;

    // 16kHz 기준 frameMs 계산은 "대략"으로 충분 (정확히는 클라가 40~60ms로 쪼갬)
    // 여기서는 bytes로부터 ms를 환산하는 보정치로 사용
    private int estimateFrameMs(int bytes) {
        // PCM16 mono: bytes = samples*2
        // samples = bytes/2
        // ms = samples / 16k * 1000
        int samples = bytes / 2;
        return (int) Math.round(samples * 1000.0 / 16000.0);
    }

    @Override
    public void run() {
        log.info("[VOICE_WORKER][START] voiceSessionId={} user={}",
                session.getVoiceSessionId(),
                session.getAuth() != null ? session.getAuth().getName() : "null");

        while (session.getRunning().get() && session.getWs().isOpen()) {
            try {
                byte[] frame = session.getAudioQueue().poll(250, TimeUnit.MILLISECONDS);
                if (frame == null) continue;

                int len = frame.length;

                // 1) 기본 검증: PCM16이므로 짝수 바이트
                if ((len & 1) == 1) {
                    log.warn("[VAD][DROP] odd bytes={} voiceSessionId={}", len, session.getVoiceSessionId());
                    continue;
                }

                // 2) 프레임 길이 범위 검증: 40~60ms => 1280~1920 bytes (기대값)
                if (len < session.getVadConfig().minFrameBytes() || len > session.getVadConfig().maxFrameBytes()) {
                    log.warn("[VAD][DROP] frameBytes={} outOfRange(1280~1920) voiceSessionId={}",
                            len, session.getVoiceSessionId());
                    continue;
                }

                long nowMs = System.currentTimeMillis();
                session.setLastFrameAtMs(nowMs);

                int frameMs = estimateFrameMs(len);
                VadDecision d = session.getVad().update(frame, nowMs, frameMs);

                // 관측 이벤트(너무 잦으면 부담이므로 필요 시 주기 제한)
                session.sendJson("""
                        {"type":"debug","kind":"vad","state":"%s","rms":%d,"isSpeech":%s}
                        """.formatted(session.getState().name(), d.rms(), d.isSpeech()));

                // barge-in: SPEAKING 중 사용자가 말하기 시작하면 TTS 즉시 취소하고 LISTENING으로 전환
                if (session.getState() == VoiceState.SPEAKING) {
                    if (d.speechStart()) {
                        session.sendJson("""
                                {"type":"debug","kind":"barge_in","action":"cancel_tts_and_listen"}
                                """);
                        session.cancelTts("barge_in");
                        session.setState(VoiceState.LISTENING);
                        session.resetUtterance();
                        session.setUtteranceStartMs(nowMs);
                        session.getUtterance().writeBytes(frame);
                        continue;
                    }
                    // SPEAKING 중에는 (일단) 음성 프레임을 누적하지 않고 barge-in 트리거만 관측
                    continue;
                }

                // 상태 전이
                if (session.getState() == VoiceState.IDLE) {
                    if (d.speechStart()) {
                        session.setState(VoiceState.LISTENING);
                        session.resetUtterance();
                        session.setUtteranceStartMs(nowMs);
                        session.getUtterance().writeBytes(frame);

                        session.sendJson("""
                                {"type":"debug","kind":"state","from":"IDLE","to":"LISTENING"}
                                """);
                    }
                    continue;
                }

                if (session.getState() == VoiceState.LISTENING) {
                    // 프레임 누적
                    session.getUtterance().writeBytes(frame);

                    // 종료 조건: (A) VAD end, (B) forceEndRequested(PTT 힌트)
                    boolean forceEnd = session.isForceEndRequested();

                    if (d.speechEnd() || forceEnd) {
                        session.setState(VoiceState.FINALIZING);
                        session.sendJson("""
                                {"type":"debug","kind":"state","from":"LISTENING","to":"FINALIZING","forceEnd":%s}
                                """.formatted(forceEnd));

                        finalizeUtterance(nowMs);
                        session.setState(VoiceState.IDLE);
                        session.sendJson("""
                                {"type":"debug","kind":"state","from":"FINALIZING","to":"IDLE"}
                                """);
                    }
                    continue;
                }

                // FINALIZING은 동기적으로 처리하고 바로 IDLE로 돌아오므로
                // 여기로 들어올 일은 거의 없음(안전장치)
                if (session.getState() == VoiceState.FINALIZING) {
                    finalizeUtterance(System.currentTimeMillis());
                    session.setState(VoiceState.IDLE);
                }

            } catch (Exception e) {
                log.warn("[VOICE_WORKER][ERR] voiceSessionId={} err={}",
                        session.getVoiceSessionId(), e.toString());
                // worker가 죽어버리면 세션이 망가짐 -> 루프 유지
            }
        }

        log.info("[VOICE_WORKER][STOP] voiceSessionId={}", session.getVoiceSessionId());
    }

    private void finalizeUtterance(long nowMs) {
        int bytes = session.getUtterance().size();
        long started = session.getUtteranceStartMs();
        long durMs = started > 0 ? (nowMs - started) : -1;

        // 너무 짧으면 버림(SoT의 “잡음 컷” 목적)
        if (durMs >= 0 && durMs < session.getVadConfig().minSpeechMs()) {
            session.sendJson("""
                    {"type":"debug","kind":"utterance_discarded","reason":"too_short","bytes":%d,"ms":%d}
                    """.formatted(bytes, durMs));
            session.resetUtterance();
            return;
        }

        // 1) 발화 확정 이벤트(관측용)
        session.sendJson("""
            {"type":"utterance_ready","bytes":%d,"ms":%d}
            """.formatted(bytes, durMs));

        // 2) STT 트리거 (PCM16 bytes를 복사해 전달)
        byte[] pcm = session.getUtterance().toByteArray();
        sttService.transcribeAsync(session, pcm, durMs);

        session.resetUtterance();
    }
}