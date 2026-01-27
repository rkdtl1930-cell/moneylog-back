package com.kbs.backend.stt;

import com.kbs.backend.voice.VoiceLlmOrchestrator;
import com.kbs.backend.websocket.voice.VoiceSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
@RequiredArgsConstructor
public class SttService {

    private final SttClient sttClient;

    // worker와 분리된 별도 풀(전사 호출은 느릴 수 있음)
    private final ExecutorService sttPool = Executors.newFixedThreadPool(4);

    // SoT 고정: 16kHz mono PCM16
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;

    private final VoiceLlmOrchestrator voiceLlmOrchestrator;

    public void transcribeAsync(VoiceSession session, byte[] utterancePcm16Le, long utteranceMs) {
        // 1) 진행 이벤트(stt_partial = progress)
        session.sendJson("""
                {"type":"stt_partial","text":"","status":"processing","ms":%d}
                """.formatted(utteranceMs));

        sttPool.submit(() -> {
            try {
                byte[] wav = WavUtil.pcm16ToWav(utterancePcm16Le, SAMPLE_RATE, CHANNELS);

                String text = sttClient.transcribeWav(wav);
                if (text == null) text = "";

                // 2) 완료 이벤트
                session.sendJson("""
                        {"type":"stt_final","text":%s,"ms":%d}
                        """.formatted(jsonEscape(text), utteranceMs));

                // 3) STT final 이후에 LLM 오케스트레이션 실행
                // (빈 텍스트면 호출하지 않는 게 안전)
                if (!text.isEmpty()) {
                    voiceLlmOrchestrator.runAsync(session, text);
                } else {
                    session.sendJson("""
                            {"type":"llm_final","reply":"","error":"empty_transcript"}
                            """);
                }

            } catch (Exception e) {
                log.warn("[STT][FAIL] voiceSessionId={} err={}", session.getVoiceSessionId(), e.toString());
                session.sendJson("""
                        {"type":"stt_final","text":"","ms":%d,"error":"stt_failed"}
                        """.formatted(utteranceMs));
            }
        });
    }

    // 매우 단순한 JSON escape (프로덕션에서는 ObjectMapper 권장)
    private String jsonEscape(String s) {
        String esc = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + esc + "\"";
    }
}