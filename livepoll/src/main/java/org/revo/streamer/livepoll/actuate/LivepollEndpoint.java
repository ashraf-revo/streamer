package org.revo.streamer.livepoll.actuate;

import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "livepoll")
public class LivepollEndpoint {

    Map<String, RtspSession> sessions;

    public LivepollEndpoint(Map<String, RtspSession> sessions) {
        this.sessions = sessions;
    }

    @ReadOperation
    public SessionsAggregatorInfo livepoll() {
        return new SessionsAggregatorInfo(this.sessions);
    }
}

