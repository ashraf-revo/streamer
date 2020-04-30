package org.revo.streamer.livepoll.actuate;

import lombok.Getter;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class SessionsAggregatorInfo {
    private int count;
    private List<SessionInfo> sessionInfos;

    public SessionsAggregatorInfo(Map<String, RtspSession> sessions) {
        this.count = sessions.size();
        this.sessionInfos = sessions.values().stream().map(SessionInfo::new).collect(Collectors.toList());
    }
}
