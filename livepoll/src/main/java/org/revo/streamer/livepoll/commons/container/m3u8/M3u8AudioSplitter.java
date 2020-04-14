package org.revo.streamer.livepoll.commons.container.m3u8;

import org.revo.streamer.livepoll.commons.container.Splitter;
import org.revo.streamer.livepoll.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.sdp.ElementSpecific;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class M3u8AudioSplitter extends Splitter {
    private ByteArrayOutputStream byteArrayOutputStream;
    private double maxParts;
    private AtomicInteger index = new AtomicInteger();

    M3u8AudioSplitter(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(requiredSeconds, elementSpecific, consumer);
        this.maxParts = Double.valueOf(Math.floor((double) (requiredSeconds * elementSpecific.getClockRate()) / (double) 1024)).intValue();
    }


    public void split(byte[] payload) {
        if (this.index.get() % maxParts == 0) {
            this.byteArrayOutputStream = new ByteArrayOutputStream();
        }

        byteArrayOutputStream.write(payload, 0, payload.length);
        if (this.index.incrementAndGet() % maxParts == 0) {
            if (this.byteArrayOutputStream != null) {
                double time = (maxParts * 1024) / (double) this.getElementSpecific().getClockRate();
                this.getConsumer().accept((int) (index.get() / maxParts), time, byteArrayOutputStream.toByteArray());
            }
        }
    }


    @Override
    public void close() {
        if (this.byteArrayOutputStream != null) {
            double lastN = index.get() - Math.floor(this.index.get() / maxParts) * maxParts;
            double time = (lastN * 1024) / (double) this.getElementSpecific().getClockRate();
            this.getConsumer().accept((int) (index.get() % maxParts), time, byteArrayOutputStream.toByteArray());
        }
    }
}
