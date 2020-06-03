package org.revo.streamer.livepoll;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;
import org.mp4parser.streaming.TrackExtension;

import java.io.FileOutputStream;
import java.io.IOException;


public class mp4parserH264TrackImplExample {
    public static void main(String[] args) throws IOException {
        String file = "/media/ashraf/WorkSpace/IdeaProjects/streamer/livepoll/target/classes/static/playlist/123/tos.h264";
        FileOutputStream fileOutputStream = new FileOutputStream("/media/ashraf/WorkSpace/IdeaProjects/streamer/livepoll/target/classes/static/playlist/123/tos.mp4");



        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(file));
        Movie m = new Movie();
        m.addTrack(h264Track);
        new FragmentedMp4Builder().build(m).writeContainer(fileOutputStream.getChannel());

    }

}

