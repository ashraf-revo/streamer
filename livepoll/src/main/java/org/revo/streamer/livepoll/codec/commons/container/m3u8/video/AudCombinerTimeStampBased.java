package org.revo.streamer.livepoll.codec.commons.container.m3u8.video;

import org.revo.streamer.livepoll.codec.commons.utils.StaticProcs;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AudCombinerTimeStampBased implements BiFunction<Long, byte[], Optional<byte[]>>, Supplier<byte[]> {
    public final static long DEFAULT_TIMESTAMP = 0L;
    private Long lastTimeStamp = 0L;
    private byte[] temp = new byte[]{};
//    private static final byte[] aud = new byte[]{0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xf0};

    @Override
    public synchronized Optional<byte[]> apply(Long timeStamp, byte[] value) {
        if (!timeStamp.equals(lastTimeStamp) && !lastTimeStamp.equals(DEFAULT_TIMESTAMP)) {
            byte[] ref = StaticProcs.copy(temp);
            temp = new byte[]{};
            temp = StaticProcs.join(temp, value);
            lastTimeStamp = timeStamp;
//            return Optional.of(StaticProcs.join(ref, aud));
            return Optional.of(ref);
        } else {
            temp = StaticProcs.join(temp, value);
            lastTimeStamp = timeStamp;
            return Optional.empty();
        }
    }

    @Override
    public byte[] get() {
//        return StaticProcs.join(this.temp, aud);
        return this.temp;
    }
}
