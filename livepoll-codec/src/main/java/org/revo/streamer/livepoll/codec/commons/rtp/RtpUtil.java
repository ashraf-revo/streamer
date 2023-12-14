package org.revo.streamer.livepoll.codec.commons.rtp;


import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;

import java.util.Base64;


public class RtpUtil {

    public static NALU toNalu(String it) {
        byte[] bytes = Base64.getDecoder().decode(it);
        return new NALU(bytes, 0, bytes.length);
    }

}
