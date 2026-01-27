package com.kbs.backend.voice;

import com.kbs.backend.service.ChatRouterClient;
import com.kbs.backend.tts.TtsService;
import com.kbs.backend.websocket.voice.VoiceSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
@RequiredArgsConstructor
public class VoiceLlmOrchestrator {

    private final ChatRouterClient chatRouterClient;
    private final TtsService ttsService;

    // STT worker와 분리된 LLM 전용 풀 (블로킹 호출(RestTemplate)이라 분리 권장)
    private final ExecutorService llmPool = Executors.newFixedThreadPool(4);

    /**
     * Step5 엔트리포인트:
     * - stt_final 텍스트가 확정되면 호출
     * - chatrouter4의 /chat 호출(tool loop 포함)
     * - WS로 llm_partial(옵션) / llm_final 송신
     */
    public void runAsync(VoiceSession session, String sttFinalText) {
        // 1) 진행 이벤트(옵션)
        session.sendJson("""
                {"type":"llm_partial","status":"processing"}
                """);

        llmPool.submit(() -> {
            long t0 = System.currentTimeMillis();
            try {
                // Authorization은 handshake에서 받은 원문 헤더를 그대로 전달하는 게 가장 안전
                String authHeader = session.getAuthHeader();

                String reply = chatRouterClient.ask(sttFinalText, authHeader);

                long dt = System.currentTimeMillis() - t0;
                session.sendJson("""
                        {"type":"llm_final","reply":%s,"latencyMs":%d}
                        """.formatted(jsonEscape(reply), dt));

                // TTS chunk 스트리밍 시작
                ttsService.speakAsync(session, reply);

            } catch (Exception e) {
                log.warn("[VOICE][LLM][FAIL] voiceSessionId={} err={}",
                        session.getVoiceSessionId(), e.toString());
                session.sendJson("""
                        {"type":"llm_final","reply":"","error":"llm_failed"}
                        """);
            }
        });
    }

    // 간단 escape (프로덕션이면 ObjectMapper 권장)
    private String jsonEscape(String s) {
        if (s == null) s = "";
        String esc = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + esc + "\"";
    }
}