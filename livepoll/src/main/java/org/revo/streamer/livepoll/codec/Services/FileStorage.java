package org.revo.streamer.livepoll.codec.Services;

import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;

public interface FileStorage {
    void store(String path, byte[] payload,boolean append);

    void append(String streamId, MediaType video, String mediaSegment);

    void write(String streamId, MediaType audio, String initSegment);
}
