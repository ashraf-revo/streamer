package org.revo.streamer.livepoll.codec.commons.rtp.d;

public enum MediaType {
    VIDEO,
    AUDIO,
    UNKOWN;
    
    public boolean isAudio() {
        return this == AUDIO;
    }
    
    public boolean isVideo() {
        return this == VIDEO;
    }
    
    public static MediaType typeOf(String type) {
        if("video".equalsIgnoreCase(type)) {
            return VIDEO;
        } else if ("audio".equalsIgnoreCase(type)) {
            return AUDIO;
        } else {
            return UNKOWN;
        }
    }
}
