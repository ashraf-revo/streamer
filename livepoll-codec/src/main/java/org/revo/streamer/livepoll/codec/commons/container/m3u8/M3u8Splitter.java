package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.audio.AacMuxer;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.H264Muxer;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.TSMuxer;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.revo.streamer.livepoll.service.FileStorage;

public class M3u8Splitter extends ContainerSplitter {

    private AacMuxer m3u8AudioSplitter;
    private H264Muxer h264Muxer;
    private TSMuxer TSMuxer;

    private final static int version = 4;

    public M3u8Splitter(int requiredSeconds, String streamId, FileStorage fileStorage, SdpElementParser sdpElementParser, TriConsumer<MediaType, Double, String> notifier) {
        super(sdpElementParser);
        if (sdpElementParser.getVideoElementSpecific() != null) {
            fileStorage.write(streamId, MediaType.VIDEO, getInitSegment(version, requiredSeconds));
        }
        if (sdpElementParser.getAudioElementSpecific() != null) {
            fileStorage.write(streamId, MediaType.AUDIO, getInitSegment(version, requiredSeconds));
        }
        if (this.getSdpElementParser().getAudioElementSpecific() != null)
            this.m3u8AudioSplitter = new AacMuxer(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
                String path = "playlist/" + streamId + "/audio" + index + ".aac";
                fileStorage.store(path, bytes, false);
                fileStorage.append(streamId, MediaType.AUDIO, getMediaSegment(time, path));
                notifier.accept(MediaType.AUDIO, time, path);
            });
        if (this.getSdpElementParser().getVideoElementSpecific() != null)
            this.h264Muxer = new H264Muxer(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
                String path = "playlist/" + streamId + "/video" + /*index +*/ ".h264";
                fileStorage.store(path, bytes, true);
                notifier.accept(MediaType.VIDEO, time, path);
            });
        if (this.getSdpElementParser().getVideoElementSpecific() != null)
            this.TSMuxer = new TSMuxer(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
                String path = "playlist/" + streamId + "/video" + /*index +*/ ".ts";
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
    public void close() {
        this.m3u8AudioSplitter.close();
        this.h264Muxer.close();
        this.TSMuxer.close();
    }

    @Override
    public void split(MediaType mediaType, long timeStamp, byte[] data) {
        if (mediaType == MediaType.AUDIO) {
            this.m3u8AudioSplitter.mux(timeStamp, data);
        }
        if (mediaType == MediaType.VIDEO) {
            this.h264Muxer.mux(timeStamp, data);
        }
        if (mediaType == MediaType.VIDEO) {
            this.TSMuxer.mux(timeStamp, data);
        }
    }
}

