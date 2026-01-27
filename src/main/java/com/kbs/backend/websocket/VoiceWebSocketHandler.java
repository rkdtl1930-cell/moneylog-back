package com.kbs.backend.websocket;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class VoiceWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, String> sessionIdByWs = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String voiceSessionId = UUID.randomUUID().toString();
        sessionIdByWs.put(session.getId(), voiceSessionId);

        Authentication auth = (Authentication) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_AUTH);

        log.info("[WS][OPEN] wsSessionId={} voiceSessionId={} auth={}",
                session.getId(), voiceSessionId, (auth != null ? auth.getName() : "null"));

        // hello_ack 전송(세션ID 내려줌)
        String payload = """
                {"type":"hello_ack","sessionId":"%s","audio":{"format":"PCM16","sampleRate":16000,"channels":1,"frameMs":50}}
                """.formatted(voiceSessionId);
        session.sendMessage(new TextMessage(payload));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String txt = message.getPayload();
        log.info("[WS][TEXT] wsSessionId={} payload={}", session.getId(), txt);

        // MVP: 그대로 ack (나중에 hello, audio_end, cancel 등 이벤트 라우팅으로 확장)
        session.sendMessage(new TextMessage("{\"type\":\"ack\",\"ok\":true}"));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        int bytes = message.getPayloadLength();
        String voiceSessionId = sessionIdByWs.get(session.getId());
        log.debug("[WS][BIN] wsSessionId={} voiceSessionId={} bytes={}",
                session.getId(), voiceSessionId, bytes);

        // 만약 sendMessage 등에서 예외가 날 수 있으면 try-catch로 감싸기
        // try { ... } catch (IOException e) { ... }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("[WS][ERR] wsSessionId={} err={}", session.getId(), exception.toString());
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String voiceSessionId = sessionIdByWs.remove(session.getId());
        log.info("[WS][CLOSE] wsSessionId={} voiceSessionId={} status={}", session.getId(), voiceSessionId, status);
    }
}

