package org.revo.streamer.livepoll.codec.commons.container.m3u8.video;

import org.revo.streamer.livepoll.codec.commons.utils.StaticProcs;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AudCombinerTimeStampBased implements BiFunction<Long, byte[], Optional<byte[]>>, Supplier<byte[]> {
    public final static long DEFAULT_TIMESTAMP = 0L;
    private Long lastTimeStamp = 0L;
    private byte[] temp = new byte[]{};

    @Override
    public synchronized Optional<byte[]> apply(Long timeStamp, byte[] value) {
        if (!timeStamp.equals(lastTimeStamp) && !lastTimeStamp.equals(DEFAULT_TIMESTAMP)) {
            byte[] ref = StaticProcs.copy(temp);
            temp = new byte[]{};
            temp = StaticProcs.join(temp, value);
            lastTimeStamp = timeStamp;
            return Optional.of(ref);
        } else {
            temp = StaticProcs.join(temp, value);
            lastTimeStamp = timeStamp;
            return Optional.empty();
        }
    }

    @Override
    public byte[] get() {
        return this.temp;
    }
}
