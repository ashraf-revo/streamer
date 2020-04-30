package org.revo.streamer.livepoll.service;

import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HolderImpl {
    @Autowired
    private FileStorage fileStorage;

    @Autowired
    private Map<String, RtspSession> sessions;

    public FileStorage getFileStorage() {
        return fileStorage;
    }

    public Map<String, RtspSession> getSessions() {
        return sessions;
    }
}
