package org.revo.streamer.livepoll.service;

import org.revo.streamer.livepoll.codec.rtsp.RtspSession;


import java.util.Map;

public class HolderImpl {
    private FileStorage fileStorage;

    private Map<String, RtspSession> sessions;

    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public void setSessions(Map<String, RtspSession> sessions) {
        this.sessions = sessions;
    }

    public FileStorage getFileStorage() {
        return fileStorage;
    }

    public Map<String, RtspSession> getSessions() {
        return sessions;
    }
}
