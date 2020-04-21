package org.revo.streamer.livepoll;

import org.junit.jupiter.api.Test;
import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;

//@SpringBootTest
class LivepollApplicationTests {

    @Test
    void contextLoads() throws IOException {
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("/media/ashraf/WorkSpace/streamer/livepoll/target/classes/static/playlist/123/video.h264"));
        Movie m = new Movie();
        m.addTrack(h264Track);

        FragmentedMp4Builder builder = new FragmentedMp4Builder();

        Container c = builder.build(m);
        c.writeContainer(new FileOutputStream("/media/ashraf/WorkSpace/streamer/livepoll/target/classes/static/playlist/123/video.mp4").getChannel());


        int pic_width_in_mbs_minus1 = 79;
        int pic_height_in_map_units_minus1 = 44;

        int frame_crop_bottom_offset = 0;
        int frame_crop_top_offset = 0;
        int frame_mbs_only_flag = 1;
        int frame_crop_right_offset = 0;
        int frame_crop_left_offset = 0;
        int Width = ((pic_width_in_mbs_minus1 + 1) * 16) - frame_crop_right_offset * 2 - frame_crop_left_offset * 2;
        int Height = ((2 - frame_mbs_only_flag) * (pic_height_in_map_units_minus1 + 1) * 16) - (frame_crop_bottom_offset * 2) - (frame_crop_top_offset * 2);
        System.out.println(Width);
        System.out.println(Height);
    }

}
