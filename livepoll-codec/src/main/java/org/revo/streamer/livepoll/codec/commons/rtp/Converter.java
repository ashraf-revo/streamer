package org.revo.streamer.livepoll.codec.commons.rtp;

public interface Converter<IN, OUT> {
    OUT convert(IN in);
}
