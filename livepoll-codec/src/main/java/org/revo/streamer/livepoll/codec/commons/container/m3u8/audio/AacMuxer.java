package org.revo.streamer.livepoll.codec.commons.container.m3u8.audio;

import org.revo.streamer.livepoll.codec.commons.container.Muxer;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class AacMuxer extends Muxer {
    private final double maxParts;
    private final AtomicInteger index = new AtomicInteger();
    private ByteArrayOutputStream byteArrayOutputStream;

    public AacMuxer(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(elementSpecific, consumer);
        this.maxParts = Double.valueOf(Math.floor((double) (requiredSeconds * elementSpecific.clockRate()) / (double) 1024)).intValue();
    }


    public void mux(long timeStamp, byte[] payload) {
        if (this.index.get() % maxParts == 0) {
            this.byteArrayOutputStream = new ByteArrayOutputStream();
        }

        byteArrayOutputStream.write(payload, 0, payload.length);
        if (this.index.incrementAndGet() % maxParts == 0) {
            if (this.byteArrayOutputStream != null) {
                double time = (maxParts * 1024) / (double) this.getElementSpecific().clockRate();
                this.getConsumer().accept((int) (index.get() / maxParts), time, byteArrayOutputStream.toByteArray());
            }
        }
    }


    @Override
    public void close() {
        if (this.byteArrayOutputStream != null) {
            double time = ((index.get() % maxParts) * 1024) / (double) this.getElementSpecific().clockRate();
            this.getConsumer().accept((int) Math.ceil(index.get() / maxParts), time, byteArrayOutputStream.toByteArray());
        }
    }
}
