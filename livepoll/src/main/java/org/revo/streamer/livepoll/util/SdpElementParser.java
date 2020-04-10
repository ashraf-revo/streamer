package org.revo.streamer.livepoll.util;

import javax.sdp.SessionDescription;
import java.util.HashMap;

public class SdpElementParser {
    private ElementSpecific audioElementSpecific;
    private ElementSpecific videoElementSpecific;

    private SdpElementParser() {
    }

    public static SdpElementParser parse(SessionDescription description) {
        SdpElementParser sdpParser = new SdpElementParser();
        sdpParser.audioElementSpecific = new ElementSpecific("aac", 48000, new HashMap<>());
        sdpParser.videoElementSpecific = new ElementSpecific("h264", 48000, new HashMap<>());
        return sdpParser;
    }

    public static boolean validate(SdpElementParser elementParser) {
        return true;
    }

    public ElementSpecific getAudioElementSpecific() {
        return audioElementSpecific;
    }

    public ElementSpecific getVideoElementSpecific() {
        return videoElementSpecific;
    }
}
