package org.revo.streamer.livepoll.codec.commons.container;

import lombok.Getter;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.io.Closeable;

@Getter
public abstract class Muxer implements Closeable {
    private final ElementSpecific elementSpecific;
    private final TriConsumer<Integer, Double, byte[]> consumer;

    public Muxer(ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        this.elementSpecific = elementSpecific;
        this.consumer = consumer;
    }

    public abstract void mux(long timeStamp, byte[] payload);

}
