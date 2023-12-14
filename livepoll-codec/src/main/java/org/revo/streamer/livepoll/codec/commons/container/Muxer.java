package org.revo.streamer.livepoll.codec.commons.container;

import java.io.Closeable;

public interface Muxer extends Closeable {

    void mux(long timeStamp, byte[] payload);

}
