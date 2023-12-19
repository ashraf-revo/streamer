package org.revo.streamer.livepoll.codec.commons.container.hls;

import io.lindstrom.m3u8.model.AlternativeRendition;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.revo.streamer.livepoll.codec.commons.container.StreamContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.container.audio.AacAudioMuxer;
import org.revo.streamer.livepoll.codec.commons.container.video.H264VideoMuxer;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.spf4j.io.PipedOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HlsStreamContainerSplitter extends StreamContainerSplitter {
    private final MasterPlaylistParser parser = new MasterPlaylistParser();
    private final HlsFileWatcher hlsFileWatcher;
    private Path baseMediaDirectory;
    private Recorder videoSubscriber;
    private Recorder audioSubscriber;
    private H264VideoMuxer h264VideoMuxer;
    private AacAudioMuxer aacAudioMuxer;


    @SneakyThrows
    public HlsStreamContainerSplitter(SdpElementParser sdpElementParser, Integer time, String streamId) {
        super(sdpElementParser);
        createBaseMediaDirectory(streamId);
        this.hlsFileWatcher = new HlsFileWatcher(this.baseMediaDirectory);
        this.createMasterPlaylist(sdpElementParser, streamId);
        if (sdpElementParser.getVideoElementSpecific() != null) {
            PipedOutputStream videoOut = new PipedOutputStream();
            videoSubscriber = new HlsMediaSubscriber(this.baseMediaDirectory, MediaType.VIDEO, time, videoOut, streamId);
            h264VideoMuxer = new H264VideoMuxer(videoOut, sdpElementParser);
        }
        if (sdpElementParser.getAudioElementSpecific() != null) {
            PipedOutputStream audioOut = new PipedOutputStream();
            audioSubscriber = new HlsMediaSubscriber(this.baseMediaDirectory, MediaType.AUDIO, time, audioOut, streamId);
            aacAudioMuxer = new AacAudioMuxer(audioOut, sdpElementParser);
        }
    }

    @Override
    public HlsStreamContainerSplitter start() {
        this.hlsFileWatcher.start();
        new Thread(videoSubscriber).start();
        new Thread(audioSubscriber).start();
        return this;
    }

    @SneakyThrows
    private void createBaseMediaDirectory(String streamId) {
        this.baseMediaDirectory = Paths.get(System.getProperty("java.io.tmpdir"), streamId);
        if (!this.baseMediaDirectory.toFile().exists()) {
            Files.createDirectory(this.baseMediaDirectory);
        } else {
            Files.list(this.baseMediaDirectory).forEach(it -> it.toFile().delete());
        }
    }


    @SneakyThrows
    private void createMasterPlaylist(SdpElementParser sdpElementParser, String streamId) {
        Path masterPlaylistPath = this.baseMediaDirectory.resolve(streamId + ".m3u8");
        MasterPlaylist playlist = MasterPlaylist.builder()
                .version(7)
                .addAlternativeRenditions(
                        AlternativeRendition.builder()
                                .type(io.lindstrom.m3u8.model.MediaType.AUDIO)
                                .name("EN")
                                .groupId("audio")
                                .defaultRendition(true)
                                .autoSelect(true)
                                .uri(streamId + ".AUDIO.m3u8")
                                .build())
                .addVariants(Variant.builder()
                        .addCodecs("avc1.42c01f")
                        .bandwidth(1100000)
                        .uri(streamId + ".VIDEO.m3u8")
                        .audio("audio")
                        .programId(1)
                        .resolution(1280, 720)
                        .build())
                .build();
        Files.write(masterPlaylistPath, parser.writePlaylistAsBytes(playlist));
    }


    @Override
    public void split(MediaType mediaType, long timeStamp, byte[] data) {
        if (mediaType.isVideo()) {
            h264VideoMuxer.mux(timeStamp, data);
        }
        if (mediaType.isAudio()) {
            aacAudioMuxer.mux(timeStamp, data);
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        this.h264VideoMuxer.close();
        this.aacAudioMuxer.close();
        this.videoSubscriber.close();
        this.audioSubscriber.close();
        Thread.sleep(100);
        //   this.fFmpegM3u8FileWatcher.close();
    }

    interface Recorder extends Closeable, Runnable {
        FFmpegFrameRecorder getRecorder(FFmpegFrameGrabber grabber, MediaType mediaType, Integer time, Path streamDirectory, String streamId);
    }

    @Slf4j
    public static class HlsMediaSubscriber implements Recorder {
        private final Path streamDirectory;
        private final MediaType mediaType;
        private final PipedOutputStream outputStream;
        private FFmpegFrameRecorder recorder;
        private FFmpegFrameGrabber grabber;
        private volatile boolean closed = false;
        private final String streamId;
        private final Integer time;

        public HlsMediaSubscriber(Path baseMediaDirectory, MediaType mediaType, Integer time, PipedOutputStream outputStream, String streamId) {
            this.streamId = streamId;
            this.streamDirectory = baseMediaDirectory.resolve(streamId + "." + mediaType.name() + ".m3u8");
            this.mediaType = mediaType;
            this.time = time;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                FFmpegLogCallback.set();
                grabber = new FFmpegFrameGrabber(outputStream.getInputStream(), 0);
                if (mediaType.isVideo()) {
                    grabber.setFormat("h264");
                }

                if (mediaType.isAudio()) {
                    grabber.setFormat("aac");
                }
                grabber.startUnsafe();
                recorder = getRecorder(grabber, mediaType, time, streamDirectory, streamId);
                Frame frame;

                while (!closed && (frame = grabber.grab()) != null) {
                    recorder.record(frame);
                }

            } catch (Exception e) {
                log.error("error in processing hls for {}", mediaType.name(), e);
            } finally {
                doClose();
            }
        }

        @SneakyThrows
        private void doClose() {
            if (grabber != null) {
                grabber.stop();
                grabber.close();
            }
            if (recorder != null) {
                recorder.flush();
                recorder.close();
            }
        }

        private static FFmpegFrameRecorder getFFmpegHlsRecorder(FFmpegFrameGrabber grabber, MediaType mediaType, Integer time, Path streamDirectory, String streamId) throws FFmpegFrameRecorder.Exception {
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(streamDirectory.toString(), grabber.getAudioChannels());

            recorder.setFormat("hls");
            recorder.setOption("hls_time", time.toString());
            recorder.setGopSize(time);
            recorder.setOption("hls_list_size", "0");
            recorder.setOption("hls_flags", "delete_segments+append_list+independent_segments");
            recorder.setOption("hls_playlist_type", "event");
            recorder.setOption("segment_list_flags", "live");
            recorder.setOption("hls_segment_type", "fmp4");
            recorder.setOption("master_pl_name", "master-" + streamId + "." + mediaType.name() + ".m3u8");
            recorder.setOption("hls_fmp4_init_filename", mediaType.name().toLowerCase() + "-init.mp4");
            if (mediaType.isVideo()) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setVideoQuality(0);
                recorder.setImageWidth(grabber.getImageWidth());
                recorder.setImageHeight(grabber.getImageHeight());
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setVideoBitrate(1000000);
            }
            if (mediaType.isAudio()) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioQuality(0);
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setAudioBitrate(128000);
            }
            recorder.startUnsafe();
            return recorder;
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
        }

        @SneakyThrows
        @Override
        public FFmpegFrameRecorder getRecorder(FFmpegFrameGrabber grabber, MediaType mediaType, Integer time, Path streamDirectory, String streamId) {
            return getFFmpegHlsRecorder(grabber, mediaType, time, streamDirectory, streamId);
        }
    }

}
