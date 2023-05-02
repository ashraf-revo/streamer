package org.revo.streamer.livepoll.mpeg2ts.commons.container.m3u8;

import org.mp4parser.streaming.input.h264.H264AnnexBTrack.NalStreamTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class M3u8SplitterTest {

    public static void main(String[] args) throws IOException {

        SimpleLocalFileStorageImpl storage = new SimpleLocalFileStorageImpl();
        Path baseDumbPath = Paths.get("livepoll-mpeg2ts", "src", "test", "resources", "static");
        TSMuxer tsMuxer = new TSMuxer(10, (var1, var2, bytes) -> {
            String path = "video-ts" + /*index +*/ ".ts";
            storage.store(path, bytes, true);
        });
        NalStreamTokenizer st = new NalStreamTokenizer(new FileInputStream(baseDumbPath.resolve("video.h264").toFile()));
        byte[] nal;
        while ((nal = st.getNext()) != null) {
            tsMuxer.mux(0, addPrefix(nal));
        }
        tsMuxer.close();

    }

    static byte[] addPrefix(byte[] nal) {
        byte[] result = new byte[nal.length + 4];
        result[0] = 0x0;
        result[1] = 0x0;
        result[2] = 0x0;
        result[3] = 0x1;
        System.arraycopy(nal, 0, result, 4, nal.length);
        return result;
    }


}

class SimpleLocalFileStorageImpl {


    public void store(String path, byte[] payload, boolean append) {
        try {
            Path staticDir = Paths.get("livepoll-mpeg2ts", "src", "test", "resources", "static");
            Path storedPath = staticDir.resolve(path);
            File stored = storedPath.toFile();
            stored.getParentFile().getParentFile().mkdir();
            stored.getParentFile().mkdir();
            try (FileOutputStream fos = new FileOutputStream(stored, append)) {
                fos.write(payload);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

record DumbStream(String mediaType, long timeStamp, String file) {
}

