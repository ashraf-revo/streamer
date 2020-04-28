package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.Codec;
import org.jcodec.common.MuxerTrack;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.container.Splitter;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.revo.streamer.livepoll.codec.sdp.SdpUtil;
import org.revo.streamer.livepoll.service.FileStorage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.jcodec.common.io.NIOUtils.writableChannel;
import static org.revo.streamer.livepoll.codec.commons.rtp.RtpUtil.toNalu;

public class M3u8Splitter extends ContainerSplitter {

    private M3u8AudioSplitter m3u8AudioSplitter;
    private M3u8VideoSplitter m3u8VideoSplitter;
    private final static int version = 4;
//    private FileChannelWrapper file;
//    private MP4Muxer muxer;

    public M3u8Splitter(int requiredSeconds, String streamId, FileStorage fileStorage, SdpElementParser sdpElementParser, TriConsumer<MediaType, Double, String> notifier) throws IOException {
        super(sdpElementParser);
        if (sdpElementParser.getVideoElementSpecific() != null) {
            fileStorage.write(streamId, MediaType.VIDEO, getInitSegment(version, requiredSeconds));
        }
        if (sdpElementParser.getAudioElementSpecific() != null) {
            fileStorage.write(streamId, MediaType.AUDIO, getInitSegment(version, requiredSeconds));
        }
        if (this.getSdpElementParser().getAudioElementSpecific() != null)
            this.m3u8AudioSplitter = new M3u8AudioSplitter(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
                String path = "playlist/" + streamId + "/audio" + index + ".aac";
                fileStorage.store(path, bytes, false);
                fileStorage.append(streamId, MediaType.AUDIO, getMediaSegment(time, path));
                notifier.accept(MediaType.AUDIO, time, path);
            });

//        this.file = writableChannel(new File("/media/ashraf/WorkSpace/streamer/livepoll/target/classes/static/playlist/123/video.mp4"));
//        this.muxer = MP4Muxer.createMP4MuxerToChannel(this.file);
//        AtomicReference<MuxerTrack> track = new AtomicReference<>();

        this.m3u8VideoSplitter = new M3u8VideoSplitter(requiredSeconds, this.getSdpElementParser().getAudioElementSpecific(), (index, time, bytes) -> {
//            int type = NALU.NaluHeader.read(bytes[4]).getTYPE();
//
//            Packet frame = new Packet(ByteBuffer.wrap(bytes), time.longValue(), 1, 1, index,
//                    ((type == 7 || type == 5) ? Packet.FrameType.KEY : Packet.FrameType.INTER), null,
//                    (type == 7 || type == 5) ? 0 : 0);
//
//            if (type == 7) {
//                track.set(muxer.addVideoTrack(Codec.H264, new H264Decoder().getCodecMeta(frame.getData())));
//            }
//            try {
//                track.get().addFrame(frame);
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }

            String path = "playlist/" + streamId + "/video" + /*index +*/ ".h264";
            fileStorage.store(path, bytes, true);
//            fileStorage.append(streamId, MediaType.VIDEO, getMediaSegment(time, path));
            notifier.accept(MediaType.VIDEO, time, path);


        });

        SdpUtil.getSpropParameter(this.getSdpElementParser().getSessionDescription())
                .stream().map(its -> Arrays.asList(its.split(",")))
                .filter(it -> it.size() == 2)
                .forEach(it -> this.m3u8VideoSplitter.setSpsPps(toNalu(it.get(0), sdpElementParser.getVideoElementSpecific()), toNalu(it.get(1), sdpElementParser.getVideoElementSpecific())));
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
//        this.muxer.finish();
//        this.file.close();

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

