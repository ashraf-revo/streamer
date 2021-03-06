package org.revo.streamer.livepoll.codec.commons.rtp;


import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.revo.streamer.livepoll.codec.commons.utils.StaticProcs.bytesToUIntInt;

public class RtpNaluEncoder implements Encoder<RtpPkt, NALU> {

    private RtpToNalu rtpToNalu = new RtpToNalu();
    private ElementSpecific specific;

    public RtpNaluEncoder(ElementSpecific specific) {
        this.specific = specific;
    }

    @Override
    public List<NALU> encode(RtpPkt rtpPkt) {
        return rtpToNalu.apply(rtpPkt);
    }

    private class RtpToNalu implements Function<RtpPkt, List<NALU>> {
        private NALU fuNalU = null;

        @Override
        public List<NALU> apply(RtpPkt rtpPkt) {
            NALU.NaluHeader naluHeader = NALU.NaluHeader.read(rtpPkt.getPayload()[0]);
            if (naluHeader.getTYPE() > 0 && naluHeader.getTYPE() <= 23) {
                return Collections.singletonList(new NALU(rtpPkt.getPayload(), 0, rtpPkt.getPayload().length,specific));
            } else if (naluHeader.getTYPE() == 24) {
                List<NALU> nalus = new ArrayList<>();
                int offset = 1;
                while (offset < rtpPkt.getPayload().length - 1 /*NAL Unit-0 Header*/) {
                    int size = bytesToUIntInt(rtpPkt.getPayload(), offset);
                    offset += 2;   //                NAL Unit-i Size
                    nalus.add(new NALU(rtpPkt.getPayload(), offset, size + offset, specific));
                    offset += size;//                NAL Unit-i Data
                }
                return nalus;
            } else if (naluHeader.getTYPE() == 28) {
                boolean start = ((rtpPkt.getPayload()[1] & 0x80) >> 7) > 0;
                boolean end = ((rtpPkt.getPayload()[1] & 0x40) >> 6) > 0;
//            int reserved = (rtpPkt.getPayload()[1] & 0x20) >> 5;
                int type = (rtpPkt.getPayload()[1] & 0x1F);
                if (start) {
                    this.fuNalU = new NALU(naluHeader.getF(), naluHeader.getNRI(), type,specific);
                    this.fuNalU.appendPayload(rtpPkt.getPayload(), 2);
                }
                if (this.fuNalU != null && this.fuNalU.getNaluHeader().getTYPE() == type) {
                    if (!start) {
                        this.fuNalU.appendPayload(rtpPkt.getPayload(), 2);
                    }
                    if (end) {
                        List<NALU> nalus = Collections.singletonList(new NALU(fuNalU.getPayload(), 0, fuNalU.getPayload().length, specific));
                        this.fuNalU = null;
                        return nalus;
                    }
                }
            } else {
                System.out.println("Unknown Type " + naluHeader.getTYPE());
            }
            return Collections.emptyList();
        }
    }

}

