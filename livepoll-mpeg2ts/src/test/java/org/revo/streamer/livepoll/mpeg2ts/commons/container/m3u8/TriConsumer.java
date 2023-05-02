package org.revo.streamer.livepoll.mpeg2ts.commons.container.m3u8;

public interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c);
}
