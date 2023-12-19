package org.revo.streamer.livepoll.codec.commons.container.video;

import lombok.SneakyThrows;
import org.revo.streamer.livepoll.codec.commons.container.Muxer;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpNALUDecoder;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.revo.streamer.livepoll.codec.sdp.SdpUtil;

import javax.sdp.SessionDescription;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.revo.streamer.livepoll.codec.commons.rtp.RtpUtil.toNalu;

public class H264VideoMuxer implements Muxer {
    private final OutputStream outputStream;
    private final SdpElementParser sdpElementParser;
    private long lastVideoTimeStamp = 0;
    private final NALU sps;
    private final NALU pps;
    private final NALU AUD_NALU = RtpNALUDecoder.AUD_NALU;

    public H264VideoMuxer(OutputStream outputStream, SdpElementParser sdpElementParser) {
        this.outputStream = outputStream;
        this.sdpElementParser = sdpElementParser;
        List<NALU> spsPps = getSpsPps(sdpElementParser.getSessionDescription());
        this.sps = spsPps.get(0);
        this.pps = spsPps.get(1);
        this.setSpsPps();
    }

    private void setSpsPps() {
        this.mux(this.lastVideoTimeStamp, this.sps.getRaw());
        this.mux(this.lastVideoTimeStamp, this.pps.getRaw());
    }

    private static List<NALU> getSpsPps(SessionDescription description) {
        return SdpUtil.getSpropParameter(description)
                .stream().map(its -> Arrays.asList(its.split(",")))
                .filter(it -> it.size() == 2)
                .flatMap(it -> Stream.of(toNalu(it.get(0)), toNalu(it.get(1))))
                .toList();
    }


    @SneakyThrows
    @Override
    public void mux(long timeStamp, byte[] payload) {
        if (lastVideoTimeStamp != timeStamp && lastVideoTimeStamp != 0) {
            outputStream.write(AUD_NALU.getRaw());
        }
        outputStream.write(payload);
        this.lastVideoTimeStamp = timeStamp;
    }


    @SneakyThrows
    @Override
    public void close() {
        this.outputStream.close();
    }
}

