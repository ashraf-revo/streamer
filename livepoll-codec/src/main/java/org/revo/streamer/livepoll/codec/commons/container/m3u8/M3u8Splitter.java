package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.audio.M3U8AudioAacMuxer;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.M3U8VideoH264Muxer;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.revo.streamer.livepoll.service.FileStorage;

public class M3u8Splitter extends ContainerSplitter {

    private M3U8AudioAacMuxer m3u8AudioSplitter;
    private final M3U8VideoH264Muxer m3U8VideoH264Muxer;
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
            this.m3u8AudioSplitter = new M3U8AudioAacMuxer(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
                String path = "playlist/" + streamId + "/audio" + index + ".aac";
                fileStorage.store(path, bytes, false);
                fileStorage.append(streamId, MediaType.AUDIO, getMediaSegment(time, path));
                notifier.accept(MediaType.AUDIO, time, path);
            });
        this.m3U8VideoH264Muxer = new M3U8VideoH264Muxer(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
            String path = "playlist/" + streamId + "/video" + /*index +*/ ".h264";
            fileStorage.store(path, bytes, true);
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
        this.m3U8VideoH264Muxer.close();
    }

    @Override
    public void split(MediaType mediaType, long timeStamp, byte[] data) {
        if (mediaType == MediaType.AUDIO) {
            this.m3u8AudioSplitter.mux(timeStamp, data);
        }
        if (mediaType == MediaType.VIDEO) {
            this.m3U8VideoH264Muxer.mux(timeStamp, data);
        }
    }
}

