package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import org.revo.streamer.livepoll.codec.commons.container.Muxer;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class M3U8AudioMuxer extends Muxer {
    private ByteArrayOutputStream byteArrayOutputStream;
    private double maxParts;
    private AtomicInteger index = new AtomicInteger();
    private long lastTimeStamp = 0;

    M3U8AudioMuxer(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(elementSpecific, consumer);
        this.maxParts = Double.valueOf(Math.floor((double) (requiredSeconds * elementSpecific.getClockRate()) / (double) 1024)).intValue();
    }


    public void mux(long timeStamp, byte[] payload) {
        this.lastTimeStamp = timeStamp;

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
            double time = ((index.get() % maxParts) * 1024) / (double) this.getElementSpecific().getClockRate();
            this.getConsumer().accept((int) Math.ceil(index.get() / maxParts), time, byteArrayOutputStream.toByteArray());
        }
    }
}
