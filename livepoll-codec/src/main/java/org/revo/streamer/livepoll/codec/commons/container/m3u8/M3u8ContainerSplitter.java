package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import io.lindstrom.m3u8.model.AlternativeRendition;
import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.spf4j.io.PipedOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class M3u8ContainerSplitter extends ContainerSplitter {
    private final PipedOutputStream videoOut = new PipedOutputStream();
    private final PipedOutputStream audioOut = new PipedOutputStream();
    private final MasterPlaylistParser parser = new MasterPlaylistParser();


    public M3u8ContainerSplitter(SdpElementParser sdpElementParser, String streamId) {
        super(sdpElementParser);
        createMasterPlaylist(sdpElementParser, streamId);
        if (sdpElementParser.getVideoElementSpecific() != null) {
            new Thread(new Subscriber(MediaType.VIDEO, videoOut, streamId)).start();
        }
        if (sdpElementParser.getAudioElementSpecific() != null) {
            new Thread(new Subscriber(MediaType.AUDIO, audioOut, streamId)).start();
        }

    }

    private void createMasterPlaylist(SdpElementParser sdpElementParser, String streamId) {
        String filename = "/media/ashraf/workspace/streamer/livepoll/target/classes/static/" + streamId + ".m3u8";
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
            Files.write(Paths.get(filename), parser.writePlaylistAsBytes(playlist));
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

    @Override
    public void close() throws IOException {
        videoOut.close();
        audioOut.close();
    }

    public static class Subscriber implements Runnable {
        private final MediaType mediaType;
        private final PipedOutputStream outputStream;
        private final String streamId;

        public Subscriber(MediaType mediaType, PipedOutputStream outputStream, String streamId) {
            this.mediaType = mediaType;
            this.outputStream = outputStream;
            this.streamId = streamId;
        }

        @Override
        public void run() {
            try {
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(outputStream.getInputStream(), 0);
                if (mediaType.isVideo()) {
                    grabber.setFormat("h264");
                }

                if (mediaType.isAudio()) {
                    grabber.setFormat("aac");
                }
                grabber.startUnsafe();
                FFmpegFrameRecorder recorder = getFFmpegHlsRecorder(grabber);
                Frame frame;

                while ((frame = grabber.grab()) != null) {
                    recorder.record(frame);
                }

                grabber.close();
                recorder.flush();
                recorder.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private FFmpegFrameRecorder getFFmpegHlsRecorder(FFmpegFrameGrabber grabber) throws FFmpegFrameRecorder.Exception {
            String filename = "/media/ashraf/workspace/streamer/livepoll/target/classes/static/" + streamId + "." + mediaType.name() + ".m3u8";
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(filename, grabber.getAudioChannels());

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

        private FFmpegFrameRecorder getFFmpegDashRecorder(FFmpegFrameGrabber grabber) throws FFmpegFrameRecorder.Exception {
            String filename = "/media/ashraf/workspace/streamer/livepoll/src/main/resources/static/dist/123/out/" + this.streamId + "-" + this.mediaType.name() + ".mpd";
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(filename, grabber.getAudioChannels());

            recorder.setFormat("dash");
            recorder.setOption("window_size", "10");
            recorder.setOption("init_seg_name", mediaType.name() + "_init_$RepresentationID$.m4s");
            recorder.setOption("media_seg_name", mediaType.name() + "_chunk_$RepresentationID$_$Number%05d$.m4s");
            recorder.setOption("streaming", "1");
            recorder.setOption("hls_playlist", "1");

            if (mediaType.isVideo()) {
//                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//                recorder.setVideoQuality(0);
                recorder.setImageWidth(grabber.getImageWidth());
                recorder.setImageHeight(grabber.getImageHeight());
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setVideoBitrate(grabber.getVideoBitrate());
                recorder.setSampleRate(grabber.getSampleRate());
            }
            if (mediaType.isAudio()) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioQuality(0);
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setAudioBitrate(grabber.getAudioBitrate());
            }
            recorder.startUnsafe();
            return recorder;
        }

    }

}
