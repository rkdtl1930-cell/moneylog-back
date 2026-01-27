package com.kbs.backend.websocket.voice;

public class RmsVad {

    private final VadConfig cfg;

    private boolean inSpeech = false;
    private long speechStartAtMs = 0;
    private long lastSpeechAtMs = 0;

    public RmsVad(VadConfig cfg) {
        this.cfg = cfg;
    }

    public VadDecision update(byte[] pcm16Frame, long nowMs, int frameMs) {
        int rms = rmsPcm16Le(pcm16Frame);

        boolean isSpeech = rms >= cfg.rmsThreshold();

        boolean speechStart = false;
        boolean speechEnd = false;

        if (!inSpeech) {
            if (isSpeech) {
                inSpeech = true;
                speechStart = true;
                speechStartAtMs = nowMs;
                lastSpeechAtMs = nowMs;
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
                    } else {
                        // 짧아서 버리지만 상태는 speech 종료로 빠져야 하므로
                        // speechEnd=false (상위에서 버퍼 discard 처리)
                    }
                }
            }
        }

        return new VadDecision(isSpeech, speechStart, speechEnd, rms);
    }

    private int rmsPcm16Le(byte[] bytes) {
        // bytes.length는 반드시 짝수여야 함
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