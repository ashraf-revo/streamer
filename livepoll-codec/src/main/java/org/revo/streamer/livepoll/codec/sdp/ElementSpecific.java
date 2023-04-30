package org.revo.streamer.livepoll.codec.sdp;

import java.util.Map;

public record ElementSpecific(String encodingName, int clockRate, Map<String, String> attr) {
}
