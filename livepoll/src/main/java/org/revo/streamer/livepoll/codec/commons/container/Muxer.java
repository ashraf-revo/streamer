package org.revo.streamer.livepoll.codec.commons.container;

import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.io.Closeable;

public abstract class Muxer implements Closeable {
    private ElementSpecific elementSpecific;
    private TriConsumer<Integer, Double, byte[]> consumer;

    public Muxer(ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        this.elementSpecific = elementSpecific;
        this.consumer = consumer;
    }

    public abstract void mux(long timeStamp, byte[] payload);

    public ElementSpecific getElementSpecific() {
        return elementSpecific;
    }

    public TriConsumer<Integer, Double, byte[]> getConsumer() {
        return consumer;
    }
}
