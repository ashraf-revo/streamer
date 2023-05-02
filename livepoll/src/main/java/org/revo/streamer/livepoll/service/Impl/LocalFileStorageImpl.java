package org.revo.streamer.livepoll.service.Impl;

import org.revo.streamer.livepoll.service.FileStorage;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class LocalFileStorageImpl implements FileStorage {
    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void store(String path, byte[] payload,boolean append) {
        try {
            File staticDir = this.resourceLoader.getResource("classpath:static").getFile();
            File stored = new File(staticDir + "/" + path);
            stored.getParentFile().getParentFile().mkdir();
            stored.getParentFile().mkdir();
            FileOutputStream fos = new FileOutputStream(stored,append);
            fos.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void append(String streamId, MediaType mediaType, String mediaSegment) {
        try {
            OutputStream os = new FileOutputStream(this.resourceLoader.getResource("classpath:static").getFile() + "/" + streamId + "." + mediaType + ".m3u8", true);
            os.write(mediaSegment.getBytes(), 0, mediaSegment.length());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void write(String streamId, MediaType mediaType, String initSegment) {
        try {
            OutputStream os = new FileOutputStream(this.resourceLoader.getResource("classpath:static").getFile() + "/" + streamId + "." + mediaType + ".m3u8", false);
            os.write(initSegment.getBytes(), 0, initSegment.length());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
