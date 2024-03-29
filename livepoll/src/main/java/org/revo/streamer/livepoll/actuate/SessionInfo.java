package org.revo.streamer.livepoll.actuate;

import lombok.Getter;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;

import java.util.Date;

@Getter
public class SessionInfo {
    private final String id;
    private final String streamId;
    private final String uri;
    private final Date createdDate;


    public SessionInfo(RtspSession rtspSession) {
        this.id = rtspSession.getId();
        this.streamId = rtspSession.getStreamId();
        this.uri = rtspSession.getUri();
        this.createdDate = rtspSession.getCreatedDate();
    }
}
