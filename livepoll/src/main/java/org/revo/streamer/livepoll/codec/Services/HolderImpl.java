package org.revo.streamer.livepoll.codec.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HolderImpl {
    @Autowired
    private FileStorage fileStorage;

    public FileStorage getFileStorage() {
        return fileStorage;
    }
}
