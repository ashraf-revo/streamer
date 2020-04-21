package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import org.revo.streamer.livepoll.codec.commons.container.Splitter;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class M3u8VideoSplitter extends Splitter {
    private AtomicInteger atomicInteger = new AtomicInteger();

    public M3u8VideoSplitter(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(requiredSeconds, elementSpecific, consumer);
    }

    @Override
    public void split(byte[] payload) {
        System.out.println("Nalu " + atomicInteger.incrementAndGet());
        this.getConsumer().accept(0, 0.0, payload);
    }

    @Override
    public void close() throws IOException {

    }

    public void setSpsPps(NALU sps, NALU pps) {
        this.split(sps.getRaw());
        this.split(pps.getRaw());
    }
}

