package org.revo.streamer.livepoll.util;

import gov.nist.javax.sdp.MediaDescriptionImpl;

import javax.sdp.SdpException;
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
                if ("audio".equals(it.getMedia().getMediaType())) {
                    String[] rtpmap = it.getAttribute("rtpmap").split(" ");
                    if (rtpmap.length > 1) {
                        String[] codec = rtpmap[1].split("/");
                        sdpParser.audioElementSpecific = new ElementSpecific(codec[0], Integer.parseInt(codec[1]), new HashMap<>());
                    }
                }
                if ("video".equals(it.getMedia().getMediaType())) {
                    String[] rtpmap = it.getAttribute("rtpmap").split(" ");
                    if (rtpmap.length > 1) {
                        String[] codec = rtpmap[1].split("/");
                        sdpParser.videoElementSpecific = new ElementSpecific(codec[0], Integer.parseInt(codec[1]), new HashMap<>());
                    }
                }

            }
        } catch (SdpException e) {
            e.printStackTrace();
        }


        return sdpParser;
    }

    public static boolean validate(SdpElementParser elementParser) {
        return elementParser.audioElementSpecific != null && elementParser.videoElementSpecific != null;
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
}
