package org.revo.streamer.livepoll.codec.commons.container.m3u8.video;

import org.revo.streamer.livepoll.codec.commons.container.Muxer;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.mpeg2ts.H264NalUtil;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.mpeg2ts.TsWriter;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TSMuxer extends Muxer {
    private final AtomicInteger atomicInteger = new AtomicInteger();
    private final TsWriter tsWriter;
    private boolean isFirstPes = true;

    public TSMuxer(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(elementSpecific, consumer);
        this.tsWriter = new TsWriter();
        int fps = 115;
        ptsIncPerFrame = (long) (1000 / fps) * 90;
        pts += ptsIncPerFrame;
        dts = pts - 200;
    }

    @Override
    public void mux(long timeStamp, byte[] payload) {
        boolean isLastFrame = NALU.NaluHeader.read(payload[4]).getTYPE() == H264NT_SPS;
        int frameType = H264NalUtil.getPesFrameType(payload);
        List<AvcFrame> encodeAvcFrames = getEncodeAvcFrames(new AvcFrame(payload, frameType, -1, getDts()), isLastFrame);
        for (AvcFrame avcFrame : encodeAvcFrames) {
            byte[] tsBuf = tsWriter.writeH264(isFirstPes, avcFrame.payload, avcFrame.pts, avcFrame.dts);
            isFirstPes = false;
            this.getConsumer().accept(atomicInteger.incrementAndGet(), (double) timeStamp, tsBuf);
        }
    }

    @Override
    public void close() {
    }


    private final ArrayDeque<AvcFrame> avcFrameCache = new ArrayDeque<AvcFrame>();

    private List<AvcFrame> getEncodeAvcFrames(AvcFrame avcFrame, boolean isLastFrame) {
        List<AvcFrame> avcFrames = new ArrayList<>();

        switch (avcFrame.frameType) {
            case FRAME_I, FRAME_P, UNSUPPORT -> {
                if (!avcFrameCache.isEmpty()) {
                    AvcFrame avcFrame2 = avcFrameCache.pop();
                    avcFrame2.pts = getPts();
                    avcFrames.add(avcFrame2);
                    while (!avcFrameCache.isEmpty())
                        avcFrames.add(avcFrameCache.pop());
                }
            }
            case FRAME_B -> avcFrame.pts = getPts();
        }

        avcFrameCache.offer(avcFrame);

        if (isLastFrame) {

            AvcFrame avcFrame2 = avcFrameCache.pop();
            avcFrame2.pts = getPts();
            avcFrames.add(avcFrame2);
            while (!avcFrameCache.isEmpty())
                avcFrames.add(avcFrameCache.pop());
        }

        return avcFrames;
    }

    static class AvcFrame {

        public byte[] payload;
        public int frameType;
        public long pts;
        public long dts;

        public AvcFrame(byte[] payload, int frameType, long pts, long dts) {
            this.payload = payload;
            this.frameType = frameType;
            this.pts = pts;
            this.dts = dts;
        }
    }


    private long getPts() {
        return pts += ptsIncPerFrame;
    }

    private long getDts() {
        return dts += ptsIncPerFrame;
    }

    protected long pts = 1L;
    protected long dts = 0L;
    protected long ptsIncPerFrame = 0;
    private static final int FRAME_I = 15;
    private static final int FRAME_P = 16;
    private static final int FRAME_B = 17;
    private static final int UNSUPPORT = -1;
    private static final int H264NT_SPS = 7;    //SPS类型值
}

