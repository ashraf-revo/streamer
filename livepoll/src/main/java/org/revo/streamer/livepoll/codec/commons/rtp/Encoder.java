package org.revo.streamer.livepoll.codec.commons.rtp;

import java.util.List;

public interface Encoder<IN, OUT> {
    List<OUT> encode(IN in);
}
