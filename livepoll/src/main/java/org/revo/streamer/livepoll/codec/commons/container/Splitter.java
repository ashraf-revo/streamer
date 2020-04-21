package org.revo.streamer.livepoll.codec.commons.container;

import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.io.Closeable;

public abstract class Splitter implements Closeable {
    private int requiredSeconds;
    private ElementSpecific elementSpecific;
    private TriConsumer<Integer, Double, byte[]> consumer;

    public Splitter(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        this.requiredSeconds = requiredSeconds;
        this.elementSpecific = elementSpecific;
        this.consumer = consumer;
    }

    public abstract void split(byte[] payload);

    public int getRequiredSeconds() {
        return requiredSeconds;
    }

    public ElementSpecific getElementSpecific() {
        return elementSpecific;
    }

    public TriConsumer<Integer, Double, byte[]> getConsumer() {
        return consumer;
    }
}
