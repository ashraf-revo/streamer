package org.revo.streamer.livepoll.commons.rtp;


import org.revo.streamer.livepoll.commons.rtp.base.AdtsFrame;
import org.revo.streamer.livepoll.commons.rtp.base.RtpPkt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.revo.streamer.livepoll.commons.utils.StaticProcs.bytesToUIntInt;

public class RtpAdtsFrameEncoder implements Encoder<RtpPkt, AdtsFrame> {
    private RtpToAdtsFrame rtpToAdtsFrame = new RtpToAdtsFrame();
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public List<AdtsFrame> encode(RtpPkt rtpPkt) {
        return rtpToAdtsFrame.apply(rtpPkt);
    }

    @Override
    public int incAndGet() {
        return atomicInteger.incrementAndGet();
    }

    private class RtpToAdtsFrame implements Function<RtpPkt, List<AdtsFrame>> {
        @Override
        public List<AdtsFrame> apply(RtpPkt rtpPkt) {
            List<AdtsFrame> adts = new ArrayList<>();
            int auHeaderLength = bytesToUIntInt(rtpPkt.getPayload(), 0) >> 3;
            int offset = 2 + auHeaderLength;
            for (int i = 0; i < (auHeaderLength / 2); i++) {
                int size = bytesToUIntInt(rtpPkt.getPayload(), 2 + (i * 2)) >> 3;
                adts.add(new AdtsFrame(rtpPkt.getPayload(), offset, size));
                offset += size;
            }
            return adts;
        }

    }
}
