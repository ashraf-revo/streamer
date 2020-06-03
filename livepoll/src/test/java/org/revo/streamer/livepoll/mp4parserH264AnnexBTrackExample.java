package org.revo.streamer.livepoll;

import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.input.h264.H264AnnexBTrack;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;
import org.mp4parser.streaming.output.mp4.StandardMp4Writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.util.Collections;

public class mp4parserH264AnnexBTrackExample {
    public static void main(String[] args) throws Exception {
        String file = "/media/ashraf/WorkSpace/IdeaProjects/streamer/livepoll/target/classes/static/playlist/123/tos.h264";
        H264AnnexBTrack h264 = new H264AnnexBTrack(new FileInputStream(file));





        FileOutputStream baos = new FileOutputStream("/media/ashraf/WorkSpace/IdeaProjects/streamer/livepoll/target/classes/static/playlist/123/tos.mp4");
        FragmentedMp4Writer writer = new FragmentedMp4Writer(Collections.singletonList(h264), Channels.newChannel(baos));
//        StandardMp4Writer writer = new StandardMp4Writer(Collections.<StreamingTrack>singletonList(h264), Channels.newChannel(baos));

//        DashFragmentedMp4Writer writer = new DashFragmentedMp4Writer(h264,new File("/media/ashraf/WorkSpace/IdeaProjects/streamer/livepoll/target/classes/static/playlist/123/df"));
//        h264.setSampleSink(writer);

        h264.call();
        writer.close();


    }

}
