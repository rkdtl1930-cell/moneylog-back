package com.kbs.backend.stt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class WavUtil {
    private WavUtil() {}

    // PCM16 little-endian, mono, 16kHz -> WAV bytes
    public static byte[] pcm16ToWav(byte[] pcm16Le, int sampleRate, int channels) {
        int byteRate = sampleRate * channels * 2; // 16-bit
        int blockAlign = channels * 2;
        int dataLen = pcm16Le.length;

        int riffChunkSize = 36 + dataLen;

        ByteBuffer buf = ByteBuffer.allocate(44 + dataLen);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.put(new byte[]{'R','I','F','F'});
        buf.putInt(riffChunkSize);
        buf.put(new byte[]{'W','A','V','E'});

        // fmt chunk
        buf.put(new byte[]{'f','m','t',' '});
        buf.putInt(16);               // PCM
        buf.putShort((short) 1);      // AudioFormat=1 (PCM)
        buf.putShort((short) channels);
        buf.putInt(sampleRate);
        buf.putInt(byteRate);
        buf.putShort((short) blockAlign);
        buf.putShort((short) 16);     // bits per sample

        // data chunk
        buf.put(new byte[]{'d','a','t','a'});
        buf.putInt(dataLen);
        buf.put(pcm16Le);

        return buf.array();
    }
}