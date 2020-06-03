package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import org.revo.streamer.livepoll.codec.commons.container.Muxer;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.AudCombinerTimeStampBased;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.utils.TriConsumer;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.mpeg2ts.H264NalUtil;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.video.mpeg2ts.TsWriter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class M3U8VideoTSMuxer extends Muxer {
    private final AtomicInteger atomicInteger = new AtomicInteger();
    private final AudCombinerTimeStampBased audCombinerTimeStampBased = new AudCombinerTimeStampBased();
    private long lastTimeStamp = 0;
    private TsWriter tsWriter;
    private boolean isFirstPes = true;
    private NALU sps;
    private NALU pps;
    private final Consumer<byte[]> consumer = it -> {
        boolean isLastFrame = NALU.NaluHeader.read(it[4]).getTYPE() == H264NT_SPS;
        int frameType = H264NalUtil.getPesFrameType(it);
        List<AvcFrame> encodeAvcFrames = getEncodeAvcFrames(new AvcFrame(it, frameType, -1, getDts()), isLastFrame);
        for (AvcFrame avcFrame : encodeAvcFrames) {
            byte[] tsBuf = tsWriter.writeH264(isFirstPes, avcFrame.payload, avcFrame.pts, avcFrame.dts);
            isFirstPes = false;
            this.getConsumer().accept(atomicInteger.incrementAndGet(), (double) this.lastTimeStamp, tsBuf);
        }
    };

    public M3U8VideoTSMuxer(int requiredSeconds, ElementSpecific elementSpecific, TriConsumer<Integer, Double, byte[]> consumer) {
        super(elementSpecific, consumer);
        this.tsWriter = new TsWriter();
        int fps = 115;
        ptsIncPerFrame = (long) (1000 / fps) * 90;
        pts += ptsIncPerFrame;
        dts = pts - 200;
    }

    @Override
    public void mux(long timeStamp, byte[] payload) {
        this.lastTimeStamp = timeStamp;
        audCombinerTimeStampBased.apply(timeStamp, payload).forEach(this.consumer);
    }

    @Override
    public void close() {
        this.audCombinerTimeStampBased.get().forEach(this.consumer);
    }

    public void setSpsPps(NALU sps, NALU pps) {
        this.sps = sps;
        this.pps = pps;
        this.mux(AudCombinerTimeStampBased.DEFAULT_TIMESTAMP, sps.getRaw());
        this.mux(AudCombinerTimeStampBased.DEFAULT_TIMESTAMP, pps.getRaw());
    }

    private ArrayDeque<AvcFrame> avcFrameCache = new ArrayDeque<AvcFrame>();

    private List<AvcFrame> getEncodeAvcFrames(AvcFrame avcFrame, boolean isLastFrame) {
        List<AvcFrame> avcFrames = new ArrayList<AvcFrame>();

        switch (avcFrame.frameType) {
            case FRAME_I:
            case FRAME_P:
            case UNSUPPORT:
                if (!avcFrameCache.isEmpty()) {
                    AvcFrame avcFrame2 = avcFrameCache.pop();
                    avcFrame2.pts = getPts();
                    avcFrames.add(avcFrame2);
                    while (!avcFrameCache.isEmpty())
                        avcFrames.add(avcFrameCache.pop());
                }
                break;

            case FRAME_B:
                avcFrame.pts = getPts();
                break;
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
        public long pts = -1;
        public long dts = -1;

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

