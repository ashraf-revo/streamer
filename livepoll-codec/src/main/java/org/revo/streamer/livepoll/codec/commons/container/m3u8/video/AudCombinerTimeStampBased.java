package org.revo.streamer.livepoll.codec.commons.container.m3u8.video;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AudCombinerTimeStampBased implements BiFunction<Long, byte[], List<byte[]>>, Supplier<List<byte[]>> {
    public final static long DEFAULT_TIMESTAMP = 0L;
    private Long lastTimeStamp = 0L;
    private List<byte[]> tt = new LinkedList<>();

    private static final byte[] aud = new byte[]{0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xf0};

    @Override
    public synchronized List<byte[]> apply(Long timeStamp, byte[] value) {
        if (!timeStamp.equals(lastTimeStamp) && !lastTimeStamp.equals(DEFAULT_TIMESTAMP)) {
            lastTimeStamp = timeStamp;
            tt.add(aud);
            List<byte[]> src = new LinkedList<>(tt);
            tt = new LinkedList<>();
            tt.add(value);
            return src;
        } else {
            tt.add(value);
            lastTimeStamp = timeStamp;
            return Collections.emptyList();
        }
    }

    @Override
    public List<byte[]> get() {
        return this.tt;
    }
}
