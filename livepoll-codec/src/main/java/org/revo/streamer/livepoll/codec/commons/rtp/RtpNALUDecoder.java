package org.revo.streamer.livepoll.codec.commons.rtp;


import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static org.revo.streamer.livepoll.codec.commons.utils.StaticProcs.bytesToUIntInt;

public class RtpNALUDecoder implements Converter<RtpPkt, List<NALU>> {
    private final int SINGLE_NALU = 23;
    private final int STAP_A = 24;
    private final int FU_A = 28;

    private final RtpToNALU rtpToNalu = new RtpToNALU();
    private final ElementSpecific specific;
    public static final NALU AUD_NALU = new NALU(new byte[]{0x09, (byte) 0xf0}, 0, 2);

    public RtpNALUDecoder(ElementSpecific specific) {
        this.specific = specific;
    }

    @Override
    public List<NALU> convert(RtpPkt rtpPkt) {
        return rtpToNalu.apply(rtpPkt);
    }

    private class RtpToNALU implements Function<RtpPkt, List<NALU>> {
        private NALU fuNalU = null;

        @Override
        public List<NALU> apply(RtpPkt rtpPkt) {
            NALU.NaluHeader naluHeader = NALU.NaluHeader.read(rtpPkt.getPayload()[0]);
            if (naluHeader.getTYPE() > 0 && naluHeader.getTYPE() <= SINGLE_NALU) {
                return List.of(new NALU(rtpPkt.getPayload(), 0, rtpPkt.getPayload().length));
            } else if (naluHeader.getTYPE() == STAP_A) {
                List<NALU> nalus = new LinkedList<>();
                int offset = 1;
                while (offset < rtpPkt.getPayload().length - 1 /*NAL Unit-0 Header*/) {
                    int size = bytesToUIntInt(rtpPkt.getPayload(), offset);
                    offset += 2;   //                NAL Unit-i Size
                    nalus.add(new NALU(rtpPkt.getPayload(), offset, size + offset));
                    offset += size;//                NAL Unit-i Data
                }
                return nalus;
            } else if (naluHeader.getTYPE() == FU_A) {
                boolean start = ((rtpPkt.getPayload()[1] & 0x80) >> 7) > 0;
                boolean end = ((rtpPkt.getPayload()[1] & 0x40) >> 6) > 0;
//            int reserved = (rtpPkt.getPayload()[1] & 0x20) >> 5;
                int type = (rtpPkt.getPayload()[1] & 0x1F);
                if (start) {
                    this.fuNalU = new NALU(naluHeader.getF(), naluHeader.getNRI(), type);
                    this.fuNalU.appendPayload(rtpPkt.getPayload(), 2);
                }
                if (this.fuNalU != null && this.fuNalU.getNALUHeader().getTYPE() == type) {
                    if (!start) {
                        this.fuNalU.appendPayload(rtpPkt.getPayload(), 2);
                    }
                    if (end) {
                        List<NALU> nalus = List.of(new NALU(fuNalU.getPayload(), 0, fuNalU.getPayload().length));
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

