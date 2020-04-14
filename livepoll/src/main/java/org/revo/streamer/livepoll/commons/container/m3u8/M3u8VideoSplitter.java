package org.revo.streamer.livepoll.commons.container.m3u8;

import org.revo.streamer.livepoll.commons.container.Splitter;
import org.revo.streamer.livepoll.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.sdp.ElementSpecific;

import java.io.IOException;

public class M3u8VideoSplitter extends Splitter {

    public M3u8VideoSplitter(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(requiredSeconds, elementSpecific, consumer);
    }

    @Override
    public void split(byte[] payload) {
        this.getConsumer().accept(0, 0.0, payload);
    }

    @Override
    public void close() throws IOException {

    }
}

