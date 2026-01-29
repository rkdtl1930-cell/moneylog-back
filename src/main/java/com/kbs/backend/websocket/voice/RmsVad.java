package com.kbs.backend.websocket.voice;

public class RmsVad {

    private final VadConfig cfg;

    private boolean inSpeech = false;
    private long speechStartAtMs = 0;
    private long lastSpeechAtMs = 0;

    // 노이즈 플로어(환경 소음) 추정: EMA
    private int noiseFloor = 0;

    // RMS 스무딩(짧은 스파이크 완화)
    private int rmsEma = 0;

    // speech start 디바운스(연속 프레임 누적)
    private int aboveStartMs = 0;

    public RmsVad(VadConfig cfg) {
        this.cfg = cfg;
    }

    public VadDecision update(byte[] pcm16Frame, long nowMs, int frameMs) {
        int rms = rmsPcm16Le(pcm16Frame);

        // 1) RMS EMA(스파이크 완화): alpha는 noiseAlpha를 재사용 (0.05 기본)
        if (rmsEma == 0) rmsEma = rms;
        rmsEma = (int) Math.round(rmsEma * (1.0 - cfg.noiseAlpha()) + rms * cfg.noiseAlpha());

        // 2) noiseFloor EMA: "말"로 보이지 않는 구간에서만 천천히 업데이트
        //    - 초기에는 첫 프레임 RMS를 seed로 사용
        if (noiseFloor == 0) noiseFloor = rmsEma;

        // 3) 동적 임계값(시작/유지) 계산
        //    - 시작 임계값: noiseFloor + margin, 단 절대 하한(rmsThreshold) 이하로는 내려가지 않음
        int dynStartTh = Math.max(cfg.rmsThreshold(), noiseFloor + cfg.noiseMargin());

        //    - 유지 임계값: 시작보다 낮게(히스테리시스) => 말 도중 끊김/토막 방지
        int dynEndTh = Math.max(cfg.rmsThreshold(), noiseFloor + Math.max(50, cfg.noiseMargin() / 2));

        boolean isSpeech = rmsEma >= (inSpeech ? dynEndTh : dynStartTh);

        boolean speechStart = false;
        boolean speechEnd = false;

        if (!inSpeech) {
            if (isSpeech) {
                // startSpeechMs 누적(연속 프레임)으로 짧은 잡음 스타트를 방지
                aboveStartMs += Math.max(1, frameMs);
                if (aboveStartMs >= cfg.startSpeechMs()) {
                    inSpeech = true;
                    speechStart = true;
                    speechStartAtMs = nowMs;
                    lastSpeechAtMs = nowMs;
                    aboveStartMs = 0;
                }
            } else {
                aboveStartMs = 0;

                // 비발화 구간에서만 noiseFloor 업데이트(단, 큰 스파이크는 제외)
                // "현재 RMS가 동적 시작 임계값보다 낮을 때"만 반영 => 주변 잡음 급증에 덜 민감
                if (rmsEma < dynStartTh) {
                    noiseFloor = (int) Math.round(noiseFloor * (1.0 - cfg.noiseAlpha()) + rmsEma * cfg.noiseAlpha());
                }
            }
        } else {
            if (isSpeech) {
                lastSpeechAtMs = nowMs;
            } else {
                // 무음이 endSilenceMs 이상이면 종료
                if (nowMs - lastSpeechAtMs >= cfg.endSilenceMs()) {
                    inSpeech = false;
                    long dur = nowMs - speechStartAtMs;

                    // 너무 짧은 발화는 speechEnd로 인정하지 않음(잡음 컷)
                    if (dur >= cfg.minSpeechMs()) {
                        speechEnd = true;
                    }
                }
            }
        }

        return new VadDecision(
                isSpeech,
                speechStart,
                speechEnd,
                rms,
                rmsEma,
                noiseFloor,
                dynStartTh,
                dynEndTh
        );
    }

    private int rmsPcm16Le(byte[] bytes) {
        long sumSq = 0;
        int n = bytes.length / 2;
        for (int i = 0; i < bytes.length; i += 2) {
            int lo = bytes[i] & 0xFF;
            int hi = bytes[i + 1]; // signed
            short sample = (short) ((hi << 8) | lo);
            int s = sample;
            sumSq += (long) s * s;
        }
        double mean = (double) sumSq / Math.max(1, n);
        return (int) Math.sqrt(mean);
    }
}
