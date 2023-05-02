package org.revo.streamer.livepoll.codec.commons.container;

import org.revo.streamer.livepoll.codec.commons.rtp.base.Packet;
import org.revo.streamer.livepoll.codec.commons.rtp.base.Raw;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;

import java.io.Closeable;
import java.util.List;

public abstract class ContainerSplitter implements Closeable {
    private final SdpElementParser sdpElementParser;

    public ContainerSplitter(SdpElementParser sdpElementParser) {
        this.sdpElementParser = sdpElementParser;
    }

    public SdpElementParser getSdpElementParser() {
        return sdpElementParser;
    }

    public abstract void split(MediaType mediaType, long timeStamp, byte[] data);

    public <T extends Packet> void split(MediaType mediaType, long timeStamp, List<T> data) {
        data.forEach(it -> split(mediaType, timeStamp, it.getRaw()));
    }
}
