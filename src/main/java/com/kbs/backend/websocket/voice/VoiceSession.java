package com.kbs.backend.websocket.voice;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@Getter
@Setter
public class VoiceSession {

    private final String voiceSessionId = UUID.randomUUID().toString();
    private final WebSocketSession ws;
    private final Authentication auth;

    private final VadConfig vadConfig;
    private final RmsVad vad;

    private final BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>(500); // 과도 적재 방지
    private final AtomicBoolean running = new AtomicBoolean(true);

    private volatile VoiceState state = VoiceState.IDLE;

    // 발화 버퍼(현재 utterance)
    private final ByteArrayOutputStream utterance = new ByteArrayOutputStream(64 * 1024);

    // push-to-talk 종료 힌트
    private volatile boolean forceEndRequested = false;

    // 관측/로그용
    private volatile long utteranceStartMs = 0;
    private volatile long lastFrameAtMs = 0;

    private final String authHeader;

    // TTS 스트림 취소용 epoch (증가하면 기존 TTS sender는 중단해야 함)
    private final AtomicLong ttsEpoch = new AtomicLong(0);

    public VoiceSession(WebSocketSession ws, Authentication auth, String authHeader, VadConfig vadConfig) {
        this.ws = ws;
        this.auth = auth;
        this.authHeader = authHeader;
        this.vadConfig = vadConfig;
        this.vad = new RmsVad(vadConfig);
    }

    public void requestForceEnd() {
        this.forceEndRequested = true;
    }

    public boolean offerFrame(byte[] frame) {
        return audioQueue.offer(frame);
    }

    public void stop() {
        running.set(false);
    }

    public void resetUtterance() {
        utterance.reset();
        forceEndRequested = false;
        utteranceStartMs = 0;
    }

    public void sendJson(String json) {
        try {
            ws.sendMessage(new TextMessage(json.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            // 상위에서 transport error로 정리하는 게 깔끔하지만,
            // 이 단계는 “로그로 확인”이 목표라서 여기서 로그만 남깁니다.
            log.warn("[WS][SEND_FAIL] voiceSessionId={} err={}", voiceSessionId, e.toString());
        }
    }

    public long nextTtsEpoch() { // 새 TTS 시작 시 호출
        return ttsEpoch.incrementAndGet();
    }

    public long currentTtsEpoch() { // 송신 루프에서 체크
        return ttsEpoch.get();
    }

    public void cancelTts(String reason) {
        long newEpoch = ttsEpoch.incrementAndGet(); // 기존 송신 루프 무효화
        sendJson("""
                {"type":"tts_cancel","ttsId":%d,"reason":%s}
                """.formatted(newEpoch, jsonEscape(reason)));
    }

    public void sendBinary(byte[] pcm16leChunk) {
        try {
            ws.sendMessage(new BinaryMessage(pcm16leChunk));
        } catch (Exception e) {
            log.warn("[WS][SEND_BIN_FAIL] voiceSessionId={} err={}", voiceSessionId, e.toString());
        }
    }

    private String jsonEscape(String s) {
        if (s == null) s = "";
        String esc = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + esc + "\"";
    }
}