package org.revo.streamer.livepoll.config.rtspHandler;

import org.revo.streamer.livepoll.commons.d.MediaType;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class HolderImpl {
    private FileOutputStream audio = new FileOutputStream("data/audio.aac");
    private FileOutputStream video = new FileOutputStream("data/video.h264");
    private AtomicInteger audioIndexAtomic = new AtomicInteger();
    private AtomicInteger videoIndexAtomic = new AtomicInteger();

    public HolderImpl() throws FileNotFoundException {
    }

    public void handel(String streamId, int seqNumber, long timeStamp, byte[] payload, MediaType mediaType) {
        try {
            if (mediaType.isAudio()) {
                System.out.println("audio " + audioIndexAtomic.incrementAndGet() + " " + payload.length);
                audio.write(payload);
            }
            if (mediaType.isVideo()) {
//                NALU.NaluHeader naluHeader = NALU.NaluHeader.read(payload[4]);
//                if (naluHeader.getTYPE() != 1) System.out.println(naluHeader.getTYPE());
                System.out.println("video " + videoIndexAtomic.incrementAndGet() + " " + payload.length);
                video.write(payload);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
