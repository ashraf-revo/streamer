package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import org.revo.streamer.livepoll.codec.commons.container.Splitter;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.AudCombinerTimeStampBased;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class M3u8VideoSplitter extends Splitter {
    private AtomicInteger atomicInteger = new AtomicInteger();
    private AudCombinerTimeStampBased audCombinerTimeStampBased = new AudCombinerTimeStampBased();
    private long lastTimeStamp = 0;

    public M3u8VideoSplitter(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(requiredSeconds, elementSpecific, consumer);
    }

    @Override
    public void split(long timeStamp, byte[] payload) {
        this.lastTimeStamp = timeStamp;
        audCombinerTimeStampBased.apply(timeStamp, payload).ifPresent(it -> {
            this.getConsumer().accept(atomicInteger.incrementAndGet(), (double) this.lastTimeStamp, it);
        });
    }

    @Override
    public void close() throws IOException {
        byte[] last = this.audCombinerTimeStampBased.get();
        this.getConsumer().accept(atomicInteger.incrementAndGet(),  (double) this.lastTimeStamp, last);
    }

    public void setSpsPps(NALU sps, NALU pps) {
        this.split(AudCombinerTimeStampBased.DEFAULT_TIMESTAMP, sps.getRaw());
        this.split(AudCombinerTimeStampBased.DEFAULT_TIMESTAMP, pps.getRaw());
    }
}

