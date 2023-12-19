package org.revo.streamer.livepoll.codec.commons.container;

import lombok.Getter;
import org.revo.streamer.livepoll.codec.commons.rtp.base.Packet;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;

import java.io.Closeable;
import java.util.List;

@Getter
public abstract class StreamContainerSplitter implements Closeable {
    private final SdpElementParser sdpElementParser;

    public StreamContainerSplitter(SdpElementParser sdpElementParser) {
        this.sdpElementParser = sdpElementParser;
    }

    public abstract void split(MediaType mediaType, long timeStamp, byte[] data);

    public <T extends Packet> void split(MediaType mediaType, long timeStamp, List<T> data) {
        data.forEach(it -> split(mediaType, timeStamp, it.getRaw()));
    }

    public StreamContainerSplitter start() {
        return this;
    }
}
