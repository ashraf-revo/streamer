package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import org.revo.streamer.livepoll.codec.commons.container.Muxer;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.AudCombinerTimeStampBased;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class M3U8VideoH264Muxer extends Muxer {
    private final AtomicInteger atomicInteger = new AtomicInteger();
    private long lastTimeStamp = 0;
    private NALU sps;
    private NALU pps;
    private final Consumer<byte[]> consumer = it -> this.getConsumer().accept(atomicInteger.incrementAndGet(), (double) this.lastTimeStamp, it);

    public M3U8VideoH264Muxer(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(elementSpecific, consumer);
    }

    @Override
    public void mux(long timeStamp, byte[] payload) {
        this.lastTimeStamp = timeStamp;
        this.consumer.accept(payload);
    }

    @Override
    public void close() {
    }

    public void setSpsPps(NALU sps, NALU pps) {
        this.sps = sps;
        this.pps = pps;
        this.mux(AudCombinerTimeStampBased.DEFAULT_TIMESTAMP, sps.getRaw());
        this.mux(AudCombinerTimeStampBased.DEFAULT_TIMESTAMP, pps.getRaw());
    }


}

