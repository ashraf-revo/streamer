package org.revo.streamer.livepoll.codec.commons.container;

import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;

import java.io.Closeable;

public abstract class ContainerSplitter implements Closeable {
    private SdpElementParser sdpElementParser;

    public ContainerSplitter(SdpElementParser sdpElementParser) {
        this.sdpElementParser = sdpElementParser;
    }

    public SdpElementParser getSdpElementParser() {
        return sdpElementParser;
    }

    public abstract void split(MediaType mediaType, long timeStamp, byte[] data);
}
