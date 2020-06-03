package org.revo.streamer.livepoll;

import org.jcodec.codecs.h264.BufferH264ES;
import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.Codec;
import org.jcodec.common.MuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.muxer.MP4Muxer;

import java.io.File;
import java.io.IOException;

import static org.jcodec.common.io.NIOUtils.writableChannel;

public class jcodecExample {
    public static void main(String[] args) throws IOException {
        args = new String[]{"/media/ashraf/WorkSpace/IdeaProjects/streamer/livepoll/target/classes/static/playlist/123/video.h264", "/media/ashraf/WorkSpace/IdeaProjects/streamer/livepoll/target/classes/static/playlist/123/video.mp4"};

        File in = new File(args[0]);
        File out = new File(args[1]);

        SeekableByteChannel file = writableChannel(out);
        MP4Muxer muxer = MP4Muxer.createMP4MuxerToChannel(file);
        MuxerTrack track = null;
        BufferH264ES es = new BufferH264ES(NIOUtils.mapFile(in));

        Packet frame;
        while ((frame = es.nextFrame()) != null) {
            if (track == null) {
                track = muxer.addVideoTrack(Codec.H264, new H264Decoder().getCodecMeta(frame.getData()));
            }
            track.addFrame(frame);
        }

        muxer.finish();

        file.close();


    }
}
