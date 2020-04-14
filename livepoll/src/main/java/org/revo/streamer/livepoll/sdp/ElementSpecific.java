package org.revo.streamer.livepoll.sdp;

import java.util.HashMap;
import java.util.Map;

public class ElementSpecific {
    private String encodingName;
    private int clockRate;
    private Map<String, String> attr = new HashMap<>();

    public ElementSpecific(String encodingName, int clockRate, Map<String, String> attr) {
        this.encodingName = encodingName;
        this.clockRate = clockRate;
        this.attr = attr;
    }

    public String getEncodingName() {
        return encodingName;
    }

    public int getClockRate() {
        return clockRate;
    }

    public Map<String, String> getAttr() {
        return attr;
    }
}
