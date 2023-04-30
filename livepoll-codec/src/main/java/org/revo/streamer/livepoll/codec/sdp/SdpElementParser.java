package org.revo.streamer.livepoll.codec.sdp;

import gov.nist.javax.sdp.MediaDescriptionImpl;

import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SdpElementParser {
    private ElementSpecific audioElementSpecific;
    private ElementSpecific videoElementSpecific;
    private SessionDescription sessionDescription;

    private SdpElementParser() {
    }

    public static SdpElementParser parse(SessionDescription sessionDescription) {
        SdpElementParser sdpParser = new SdpElementParser();
        sdpParser.sessionDescription = sessionDescription;
        try {
            for (MediaDescriptionImpl it : ((ArrayList<MediaDescriptionImpl>) Collections.list(sessionDescription
                    .getMediaDescriptions(true).elements()))) {
                if ("audio".equals(it.getMedia().getMediaType()) && it.getAttribute("rtpmap") != null) {
                    sdpParser.audioElementSpecific = parseRtpmap(it);
                }
                if ("video".equals(it.getMedia().getMediaType()) && it.getAttribute("rtpmap") != null) {
                    sdpParser.videoElementSpecific = parseRtpmap(it);
                }

            }
        } catch (SdpException e) {
            e.printStackTrace();
        }


        return sdpParser;
    }


    public static boolean validate(SdpElementParser elementParser) {
        return elementParser.audioElementSpecific != null || elementParser.videoElementSpecific != null;
    }

    public ElementSpecific getAudioElementSpecific() {
        return audioElementSpecific;
    }

    public ElementSpecific getVideoElementSpecific() {
        return videoElementSpecific;
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    private static ElementSpecific parseRtpmap(MediaDescriptionImpl it) throws SdpParseException {
        String[] rtpmap = it.getAttribute("rtpmap").split(" ");
        if (rtpmap.length > 1) {
            String[] codec = rtpmap[1].split("/");
            return new ElementSpecific(codec[0], Integer.parseInt(codec[1]), new HashMap<>());
        }
        return null;
    }
}
