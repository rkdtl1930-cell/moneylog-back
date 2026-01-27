package com.kbs.backend.websocket.voice;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.*;
import com.kbs.backend.stt.SttService;


@Log4j2
@Component
@RequiredArgsConstructor
public class VoiceSessionManager {

    private final Map<String, VoiceSession> sessions = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final VadConfig vadConfig = VadConfig.defaultConfig();

    private final SttService sttService;

    public VoiceSession onOpen(WebSocketSession ws, Authentication auth, String authHeader) {
        VoiceSession s = new VoiceSession(ws, auth, authHeader, vadConfig);
        sessions.put(ws.getId(), s);
        executor.submit(new VoiceAudioWorker(s, sttService));

        log.info("[VOICE_SESSION][OPEN] wsSessionId={} voiceSessionId={} user={}",
                ws.getId(), s.getVoiceSessionId(), auth != null ? auth.getName() : "null");

        return s;
    }

    public void onClose(WebSocketSession ws) {
        VoiceSession s = sessions.remove(ws.getId());
        if (s != null) {
            s.stop();
            log.info("[VOICE_SESSION][CLOSE] wsSessionId={} voiceSessionId={}", ws.getId(), s.getVoiceSessionId());
        }
    }

    public void onText(WebSocketSession ws, String payload) {
        VoiceSession s = sessions.get(ws.getId());
        if (s == null) return;

        // 최소 구현: audio_end만 처리
        if (payload.contains("\"type\":\"audio_end\"") || payload.contains("\"type\": \"audio_end\"")) {
            s.requestForceEnd();
            s.sendJson("{\"type\":\"debug\",\"kind\":\"audio_end_received\"}");
        }
        // hello/ping 등은 다음 단계에서 확장
    }

    public void onBinary(WebSocketSession ws, byte[] frame) {
        VoiceSession s = sessions.get(ws.getId());
        if (s == null) return;

        boolean ok = s.offerFrame(frame);
        if (!ok) {
            log.warn("[VOICE_SESSION][DROP] queue_full wsSessionId={} voiceSessionId={}",
                    ws.getId(), s.getVoiceSessionId());
        }
    }

    public VoiceSession getByWsId(String wsSessionId) {
        return sessions.get(wsSessionId);
    }
}
