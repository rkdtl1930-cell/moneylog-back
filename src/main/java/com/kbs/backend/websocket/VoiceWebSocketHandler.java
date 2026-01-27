package com.kbs.backend.websocket;

import com.kbs.backend.websocket.voice.VoiceSession;
import com.kbs.backend.websocket.voice.VoiceSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class VoiceWebSocketHandler extends AbstractWebSocketHandler {

    private final VoiceSessionManager voiceSessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Authentication auth = (Authentication) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_AUTH);
        String authHeader = (String) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_AUTH_HEADER);

        VoiceSession vs = voiceSessionManager.onOpen(session, auth, authHeader);

        // hello_ack
        String payload = """
                {"type":"hello_ack","sessionId":"%s","audio":{"format":"PCM16","sampleRate":16000,"channels":1,"frameMs":"40~60"}}
                """.formatted(vs.getVoiceSessionId());

        try {
            session.sendMessage(new TextMessage(payload));
        } catch (Exception e) {
            log.warn("[WS][HELLO_ACK_FAIL] wsSessionId={} err={}", session.getId(), e.toString());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        voiceSessionManager.onText(session, message.getPayload());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // 수신만 하고 처리는 worker가 수행
        var buf = message.getPayload();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        voiceSessionManager.onBinary(session, bytes);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("[WS][ERR] wsSessionId={} err={}", session.getId(), exception.toString());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception ignored) {}
        voiceSessionManager.onClose(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[WS][CLOSE] wsSessionId={} status={}", session.getId(), status);
        voiceSessionManager.onClose(session);
    }
}