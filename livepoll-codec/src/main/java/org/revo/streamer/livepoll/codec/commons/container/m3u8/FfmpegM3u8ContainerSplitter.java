package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import io.lindstrom.m3u8.model.AlternativeRendition;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.spf4j.io.PipedOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FfmpegM3u8ContainerSplitter extends ContainerSplitter {
    private final PipedOutputStream videoOut = new PipedOutputStream();
    private final PipedOutputStream audioOut = new PipedOutputStream();
    private final MasterPlaylistParser parser = new MasterPlaylistParser();
    private final FFmpegM3u8FileWatcher fFmpegM3u8FileWatcher;
    private final Path baseMediaDirectory;
    private HlsMediaSubscriber videoSubscriber;
    private HlsMediaSubscriber audioSubscriber;


    @SneakyThrows
    public FfmpegM3u8ContainerSplitter(SdpElementParser sdpElementParser, String streamId) {
        super(sdpElementParser);
        this.baseMediaDirectory = Files.createTempDirectory("stream-" + streamId);
        this.fFmpegM3u8FileWatcher = new FFmpegM3u8FileWatcher(this.baseMediaDirectory);
        this.fFmpegM3u8FileWatcher.start();
        this.createMasterPlaylist(this.baseMediaDirectory, sdpElementParser, streamId);
        if (sdpElementParser.getVideoElementSpecific() != null) {
            videoSubscriber = new HlsMediaSubscriber(this.baseMediaDirectory, MediaType.VIDEO, videoOut, streamId);
            new Thread(videoSubscriber).start();
        }
        if (sdpElementParser.getAudioElementSpecific() != null) {
            audioSubscriber = new HlsMediaSubscriber(this.baseMediaDirectory, MediaType.AUDIO, audioOut, streamId);
            new Thread(audioSubscriber).start();
        }

    }

    private void createMasterPlaylist(Path baseMediaDirectory, SdpElementParser sdpElementParser, String streamId) {
        Path masterPlaylistPath = baseMediaDirectory.resolve(streamId + ".m3u8");
        MasterPlaylist playlist = MasterPlaylist.builder()
                .addAlternativeRenditions(AlternativeRendition.builder()
                        .type(io.lindstrom.m3u8.model.MediaType.AUDIO)
                        .name("EN")
                        .groupId("audio")
                        .defaultRendition(true)
                        .autoSelect(true)
                        .uri(streamId + ".AUDIO.m3u8")
                        .build())
                .addVariants(
                        Variant.builder()
                                .addCodecs("avc1.4d401e", "mp4a.40.2", "opus")
                                .bandwidth(646043)
                                .uri(streamId + ".VIDEO.m3u8")
                                .audio("audio")
                                .programId(1)
                                .resolution(640, 360)
                                .build())
                .build();
        try {
            Files.write(masterPlaylistPath, parser.writePlaylistAsBytes(playlist));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void split(MediaType mediaType, long timeStamp, byte[] data) {
        try {
            if (mediaType.isVideo()) {
                videoOut.write(data);
            }
            if (mediaType.isAudio()) {
                audioOut.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        this.videoOut.close();
        this.audioOut.close();
        this.videoSubscriber.close();
        this.audioSubscriber.close();
        Thread.sleep(100);
        this.fFmpegM3u8FileWatcher.close();
    }


    @Slf4j
    public static class HlsMediaSubscriber implements Runnable, Closeable {
        private final Path streamDirectory;
        private final MediaType mediaType;
        private final PipedOutputStream outputStream;
        private FFmpegFrameRecorder recorder;
        private FFmpegFrameGrabber grabber;
        private volatile boolean closed = false;

        public HlsMediaSubscriber(Path baseMediaDirectory, MediaType mediaType, PipedOutputStream outputStream, String streamId) {
            this.streamDirectory = baseMediaDirectory.resolve(streamId + "." + mediaType.name() + ".m3u8");
            this.mediaType = mediaType;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                grabber = new FFmpegFrameGrabber(outputStream.getInputStream(), 0);
                if (mediaType.isVideo()) {
                    grabber.setFormat("h264");
                }

                if (mediaType.isAudio()) {
                    grabber.setFormat("aac");
                }
                grabber.startUnsafe();
                recorder = getFFmpegHlsRecorder(grabber, mediaType, streamDirectory);
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

        private static FFmpegFrameRecorder getFFmpegHlsRecorder(FFmpegFrameGrabber grabber, MediaType mediaType, Path streamDirectory) throws FFmpegFrameRecorder.Exception {
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(streamDirectory.toString(), grabber.getAudioChannels());

            recorder.setFormat("hls");
            recorder.setOption("hls_time", "10");
            recorder.setOption("hls_list_size", "10");
            recorder.setOption("hls_flags", "delete_segments+append_list+split_by_time");
            recorder.setOption("hls_segment_type", "fmp4");
            recorder.setOption("master_pl_name", "master-" + mediaType.name() + ".m3u8");
            if (mediaType.isVideo()) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setVideoQuality(0);
                recorder.setImageWidth(grabber.getImageWidth());
                recorder.setImageHeight(grabber.getImageHeight());
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setVideoBitrate(grabber.getVideoBitrate());
                recorder.setSampleRate(grabber.getSampleRate());
                recorder.setOption("hls_fmp4_init_filename", "video-init.mp4");
            }
            if (mediaType.isAudio()) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioQuality(0);
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setAudioBitrate(grabber.getAudioBitrate());
                recorder.setOption("hls_fmp4_init_filename", "audio-init.mp4");
            }
            recorder.startUnsafe();
            return recorder;
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
        }
    }

}
