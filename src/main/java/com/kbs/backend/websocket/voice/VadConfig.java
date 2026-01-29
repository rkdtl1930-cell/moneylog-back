package com.kbs.backend.websocket.voice;

import lombok.Builder;

@Builder
public record VadConfig(
        int minFrameBytes,      // 40ms 기준: 1280 bytes
        int maxFrameBytes,      // 60ms 기준: 1920 bytes

        int rmsThreshold,       // "절대" 에너지 하한. 동적 임계값이 이보다 낮아지지 않음(기기별 튜닝)
        int startSpeechMs,      // 이만큼 연속(누적)으로 임계값을 넘으면 speechStart (짧은 잡음 스타트 방지)

        int minSpeechMs,        // 최소 발화 길이 (너무 짧은 잡음 컷)
        int endSilenceMs,       // 이만큼 무음이면 발화 종료

        int noiseMargin,        // noiseFloor 대비 얼마나 커야 "말"로 보나(동적 임계값 오프셋)
        double noiseAlpha       // noiseFloor/RMS EMA 업데이트 비율 (0~1). 작을수록 완만
) {
    public static VadConfig defaultConfig() {
        return VadConfig.builder()
                .minFrameBytes(1280)
                .maxFrameBytes(1920)

                .rmsThreshold(900)   // 절대 하한(기기별로 달라질 수 있음)
                .startSpeechMs(120)  // 50ms 프레임 기준 2~3프레임 누적

                .minSpeechMs(250)
                .endSilenceMs(600)

                .noiseMargin(350)    // "주변 소음" 대비 이 정도는 더 커야 speech로 인정
                .noiseAlpha(0.05)    // noiseFloor는 천천히 따라오게(잡음 스파이크에 둔감)
                .build();
    }
}

