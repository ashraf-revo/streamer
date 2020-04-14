package org.revo.streamer.livepoll.commons.rtp;


import org.revo.streamer.livepoll.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.sdp.ElementSpecific;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static org.revo.streamer.livepoll.commons.utils.StaticProcs.uIntIntToByteWord;


public class RtpUtil {
    private static byte[] header = new byte[]{(byte) 0x80, (byte) 0x60, (byte) 0x0b, (byte) 0x89, (byte) 0xbf, (byte) 0x65, (byte) 0x75, (byte) 0x88, (byte) 0x38, (byte) 0x43, (byte) 0xf2, (byte) 0x04};

    public static RtpPkt fromNalu(NALU nalu) {
        byte[] rtpRaw = new byte[nalu.getPayload().length + header.length];
        System.arraycopy(header, 0, rtpRaw, 0, header.length);
        System.arraycopy(nalu.getPayload(), 0, rtpRaw, header.length, nalu.getPayload().length);
        return new RtpPkt(0, rtpRaw);
    }

    public static NALU spsppsToNalu(List<String> spspps, ElementSpecific specific) {
        NALU fuNalU = new NALU(0, 3, 24,specific);
        for (String s : spspps) {
            byte[] data = Base64.getDecoder().decode(s);
            fuNalU.appendPayload(uIntIntToByteWord(data.length), 0);
            fuNalU.appendPayload(data, 0);
        }
        return fuNalU;
    }

    public static List<NALU> spsppsToNalus(List<String> spspps, ElementSpecific specific) {
        return spspps.stream().map((String it) -> toNalu(it,specific)).collect(Collectors.toList());
    }

    public static NALU toNalu(String it, ElementSpecific specific) {
        byte[] bytes = Base64.getDecoder().decode(it);
        return new NALU(bytes, 0, bytes.length, specific);
    }

    public static RtpPkt toRtpPkt(String spspps, ElementSpecific specific) {
        return fromNalu(spsppsToNalu(Arrays.asList(spspps.split(",")),specific));
    }
}
