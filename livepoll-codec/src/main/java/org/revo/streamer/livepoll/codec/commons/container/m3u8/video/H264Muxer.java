package org.revo.streamer.livepoll.codec.commons.container.m3u8.video;

import org.revo.streamer.livepoll.codec.commons.container.Muxer;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.util.concurrent.atomic.AtomicInteger;

public class H264Muxer extends Muxer {
    private final AtomicInteger atomicInteger = new AtomicInteger();

    public H264Muxer(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(elementSpecific, consumer);
    }

    @Override
    public void mux(long timeStamp, byte[] payload) {
        this.getConsumer().accept(atomicInteger.incrementAndGet(), (double) timeStamp, payload);
    }

    @Override
    public void close() {
    }
}
