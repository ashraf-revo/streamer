package org.revo.streamer.livepoll.commons.container.m3u8;

import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import org.revo.streamer.livepoll.Services.FileStorage;
import org.revo.streamer.livepoll.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.commons.container.Splitter;
import org.revo.streamer.livepoll.commons.rtp.RtpUtil;
import org.revo.streamer.livepoll.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.sdp.SdpElementParser;
import org.revo.streamer.livepoll.sdp.SdpUtil;

import java.io.IOException;
import java.util.Arrays;

public class M3u8Splitter extends ContainerSplitter {

    private M3u8AudioSplitter m3u8AudioSplitter;
    private M3u8VideoSplitter m3u8VideoSplitter;
    private final int version = 4;

    public M3u8Splitter(int requiredSeconds, String streamId, FileStorage fileStorage, SdpElementParser sdpElementParser, TriConsumer<MediaType, Double, String> notifier) {
        super(sdpElementParser);
        if (sdpElementParser.getVideoElementSpecific() != null) {
            fileStorage.write(streamId, MediaType.AUDIO, getInitSegment(version, requiredSeconds));
            fileStorage.write(streamId, MediaType.VIDEO, getInitSegment(version, requiredSeconds));
            String tpath = "playlist/" + streamId + "/video" + /*index +*/ ".h264";


            fileStorage.store(tpath, "".getBytes(), false);
            SdpUtil.getSpropParameter(this.getSdpElementParser().getSessionDescription())
                    .stream().map(its -> Arrays.asList(its.split(",")))
                    .filter(it -> it.size() == 2)
                    .forEach(it -> {
                        System.out.println(RtpUtil.toNalu(it.get(0), sdpElementParser.getVideoElementSpecific()).getNaluHeader().getTYPE());
                        System.out.println(RtpUtil.toNalu(it.get(1), sdpElementParser.getVideoElementSpecific()).getNaluHeader().getTYPE());
                        fileStorage.store(tpath, RtpUtil.toNalu(it.get(0), sdpElementParser.getVideoElementSpecific()).getRaw(), true);
                        fileStorage.store(tpath, RtpUtil.toNalu(it.get(1), sdpElementParser.getVideoElementSpecific()).getRaw(), true);
                    });
        }
        if (this.getSdpElementParser().getAudioElementSpecific() != null)
            this.m3u8AudioSplitter = new M3u8AudioSplitter(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
                String path = "playlist/" + streamId + "/audio" + index + ".aac";
                fileStorage.store(path, bytes, false);
                fileStorage.append(streamId, MediaType.AUDIO, getMediaSegment(time, path));
                notifier.accept(MediaType.AUDIO, time, path);
            });
        this.m3u8VideoSplitter = new M3u8VideoSplitter(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
            String path = "playlist/" + streamId + "/video" + /*index +*/ ".h264";
            fileStorage.store(path, bytes, true);
//            fileStorage.append(streamId, MediaType.VIDEO, getMediaSegment(time, path));
            notifier.accept(MediaType.VIDEO, time, path);
        });
    }


    private String getMediaSegment(double duration, String uri) {
        return "#EXTINF:" + duration + ",\n" + uri + "\n";
    }

    private String getInitSegment(int version, int targetDuration) {
        MediaPlaylistParser parser = new MediaPlaylistParser();
        return parser.writePlaylistAsString(MediaPlaylist.builder()
                .version(version)
                .targetDuration(targetDuration)
                .mediaSequence(1)
                .ongoing(true).build());

    }

    @Override
    public void close() throws IOException {
        this.m3u8AudioSplitter.close();
        this.m3u8VideoSplitter.close();

    }

    @Override
    public Splitter getM3u8AudioSplitter() {
        return this.m3u8AudioSplitter;
    }

    @Override
    public Splitter getVideoSlitter() {
        return this.m3u8VideoSplitter;
    }
}

