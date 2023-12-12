package org.revo.streamer.livepoll.service;

import lombok.Getter;
import lombok.Setter;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;

import java.util.Map;

@Getter
@Setter
public class HolderImpl {
    private Map<String, RtspSession> sessions;
}
