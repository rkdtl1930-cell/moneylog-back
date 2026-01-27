package com.kbs.backend.tts;

import com.kbs.backend.websocket.voice.VoiceSession;
import com.kbs.backend.websocket.voice.VoiceState;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TtsService {

    private final TtsClient ttsClient;

    private final ExecutorService ttsPool = Executors.newFixedThreadPool(2);

    // 16kHz mono PCM16
    private static final int SAMPLE_RATE = 16000;

    // chunk 송신은 “대략 50ms” 기준으로 자름(SoT: 40~60ms)
    // 16kHz mono PCM16: 1초 = 16000 samples = 32000 bytes
    // 50ms = 0.05s => 1600 bytes
    private static final int CHUNK_BYTES_50MS = (int) (SAMPLE_RATE * 0.05 * 2);

    public void speakAsync(VoiceSession session, String text) {
        if (text == null) text = "";

        long myEpoch = session.nextTtsEpoch();

        // SPEAKING 전환(상태머신 관측용)
        session.setState(VoiceState.SPEAKING);

        session.sendJson("""
                {"type":"tts_start","ttsId":%d,"format":"PCM16","sampleRate":16000,"channels":1}
                """.formatted(myEpoch));

        String finalText = text;
        ttsPool.submit(() -> {
            try {
                byte[] pcm = ttsClient.synthesizePcm16(finalText);
                if (pcm == null) pcm = new byte[0];

                // chunk 송신 루프 (취소 토큰 체크)
                int off = 0;
                while (off < pcm.length) {
                    // barge-in 등으로 epoch가 바뀌면 즉시 중단
                    if (session.currentTtsEpoch() != myEpoch) {
                        log.info("[TTS][CANCELLED] voiceSessionId={} ttsId={}", session.getVoiceSessionId(), myEpoch);
                        // 상태는 IDLE로 복귀시키되, LISTENING으로 이미 넘어갔을 수 있음 -> 덮어쓰지 않음
                        if (session.getState() == VoiceState.SPEAKING) session.setState(VoiceState.IDLE);
                        return;
                    }

                    int n = Math.min(CHUNK_BYTES_50MS, pcm.length - off);
                    byte[] chunk = new byte[n];
                    System.arraycopy(pcm, off, chunk, 0, n);
                    off += n;

                    session.sendBinary(chunk);
                }

                // 끝
                session.sendJson("""
                        {"type":"tts_end","ttsId":%d}
                        """.formatted(myEpoch));

                if (session.getState() == VoiceState.SPEAKING) session.setState(VoiceState.IDLE);

            } catch (Exception e) {
                log.warn("[TTS][FAIL] voiceSessionId={} err={}", session.getVoiceSessionId(), e.toString());
                session.sendJson("""
                        {"type":"tts_end","ttsId":%d,"error":"tts_failed"}
                        """.formatted(myEpoch));
                if (session.getState() == VoiceState.SPEAKING) session.setState(VoiceState.IDLE);
            }
        });
    }
}