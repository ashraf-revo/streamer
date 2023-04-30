package org.revo.streamer.livepoll.codec.commons.rtp;

import java.util.List;

public interface Decoder<IN, OUT> {
    List<OUT> decode(IN in);
}
