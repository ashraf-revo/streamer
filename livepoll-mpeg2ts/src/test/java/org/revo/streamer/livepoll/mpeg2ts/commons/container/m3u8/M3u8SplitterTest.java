package org.revo.streamer.livepoll.mpeg2ts.commons.container.m3u8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class M3u8SplitterTest {

    public static void main(String[] args) throws IOException {

        int streamId = 555;
        SimpleLocalFileStorageImpl storage = new SimpleLocalFileStorageImpl();
        Path baseDumbPath = Paths.get("livepoll-ts", "src", "test", "resources", "static", "dumb");
        List<String> dumb = Files.readAllLines(baseDumbPath.resolve("index.txt"));
        TSMuxer tsMuxer = new TSMuxer(10, (var1, var2, bytes) -> {
            String path = "playlist/" + streamId + "/video" + /*index +*/ ".ts";
            storage.store(path, bytes, true);
        });
        streamH264NALUFiles(baseDumbPath, dumb, tsMuxer);
        tsMuxer.close();
    }

    private static void streamH264NALUFiles(Path baseDumbPath, List<String> dumb, TSMuxer tsMuxer) {
        dumb.stream()
                .map(it -> it.split("-"))
                .filter(it -> it.length == 3)
                .map(it -> new DumbStream(it[0], Long.parseLong(it[1]), it[2]))
                .forEach(it -> {
                    try {
                        if (it.mediaType().equals("VIDEO")) {
                            byte[] bytes = Files.readAllBytes(baseDumbPath.resolve(it.file()));
                            tsMuxer.mux(it.timeStamp(), bytes);
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
    }


}

class SimpleLocalFileStorageImpl {


    public void store(String path, byte[] payload, boolean append) {
        try {
            Path staticDir = Paths.get("livepoll-codec", "src", "test", "resources", "static");
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

    public void append(String streamId, String mediaType, String mediaSegment) {
        try {
            Path staticDir = Paths.get("livepoll-codec", "src", "test", "resources", "static");
            Path storedPath = staticDir.resolve(streamId + "." + mediaType + ".m3u8");
            File stored = storedPath.toFile();
            OutputStream os = new FileOutputStream(stored, true);
            os.write(mediaSegment.getBytes(), 0, mediaSegment.length());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void write(String streamId, String mediaType, String initSegment) {
        try {
            Path staticDir = Paths.get("livepoll-codec", "src", "test", "resources", "static");
            Path storedPath = staticDir.resolve(streamId + "." + mediaType + ".m3u8");
            File stored = storedPath.toFile();
            OutputStream os = new FileOutputStream(stored, false);
            os.write(initSegment.getBytes(), 0, initSegment.length());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

record DumbStream(String mediaType, long timeStamp, String file) {
}

