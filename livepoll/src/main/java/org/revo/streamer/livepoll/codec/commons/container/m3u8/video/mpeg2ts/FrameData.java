package org.revo.streamer.livepoll.codec.commons.container.m3u8.video.mpeg2ts;

//
public class FrameData {

    public boolean isAudio;            // h264 | aac
    public byte[] buf;
    public long pts;
    public long dts;
}
