package org.revo.streamer.livepoll.codec.commons.rtp;


import org.revo.streamer.livepoll.codec.commons.rtp.base.Adts;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.revo.streamer.livepoll.codec.commons.utils.StaticProcs.bytesToUIntInt;

public class RtpAdtsEncoder implements Encoder<RtpPkt, Adts> {
    private RtpToAdtsFrame rtpToAdtsFrame = new RtpToAdtsFrame();
    private ElementSpecific specific;

    public RtpAdtsEncoder(ElementSpecific specific) {
        this.specific = specific;
    }

    @Override
    public List<Adts> encode(RtpPkt rtpPkt) {
        return rtpToAdtsFrame.apply(rtpPkt);
    }

    private class RtpToAdtsFrame implements Function<RtpPkt, List<Adts>> {
        @Override
        public List<Adts> apply(RtpPkt rtpPkt) {
            List<Adts> adts = new ArrayList<>();
            int auHeaderLength = bytesToUIntInt(rtpPkt.getPayload(), 0) >> 3;
            int offset = 2 + auHeaderLength;
            for (int i = 0; i < (auHeaderLength / 2); i++) {
                int size = bytesToUIntInt(rtpPkt.getPayload(), 2 + (i * 2)) >> 3;
                adts.add(new Adts(rtpPkt.getPayload(), offset, size, specific));
                offset += size;
            }
            return adts;
        }

    }
}
