package com.kbs.backend.tts;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiTtsClient implements TtsClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.tts.endpoint:https://api.openai.com/v1/audio/speech}")
    private String endpoint;

    @Value("${openai.tts.model:gpt-4o-mini-tts}")
    private String model;

    @Value("${openai.tts.voice:alloy}")
    private String voice;

    @Value("${openai.tts.format:pcm}") // 구현/운영에서 바뀔 수 있으니 설정으로 고정
    private String format;

    @Override
    public byte[] synthesizePcm16(String text) {
        WebClient webClient = webClientBuilder
                .baseUrl(endpoint.replace("/v1/audio/speech", ""))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        // 요청 바디는 "모델/보이스/입력/포맷"만 최소로 둠
        Map<String, Object> body = Map.of(
                "model", model,
                "voice", voice,
                "input", text,
                "format", format
        );

        return webClient.post()
                .uri("/v1/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(60))
                .block();
    }
}