package org.revo.streamer.livepoll.commons.container;

import org.revo.streamer.livepoll.sdp.SdpElementParser;

import java.io.Closeable;

public abstract class ContainerSplitter implements Closeable {
    private SdpElementParser sdpElementParser;

    public ContainerSplitter(SdpElementParser sdpElementParser) {
        this.sdpElementParser = sdpElementParser;
    }

    public SdpElementParser getSdpElementParser() {
        return sdpElementParser;
    }

    public abstract Splitter getM3u8AudioSplitter();

    public abstract Splitter getVideoSlitter();
}
