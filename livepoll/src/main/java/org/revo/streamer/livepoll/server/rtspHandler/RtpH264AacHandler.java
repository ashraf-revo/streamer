package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.Converter;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpADTSDecoder;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpNALUDecoder;
import org.revo.streamer.livepoll.codec.commons.rtp.base.ADTS;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.rtp.base.Raw;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.commons.rtp.d.InterLeavedRTPSession;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;
import org.revo.streamer.livepoll.codec.sdp.SdpUtil;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static org.revo.streamer.livepoll.codec.commons.rtp.RtpUtil.toNalu;

public class RtpH264AacHandler implements BiFunction<RtpPkt, RtspSession, Mono<DefaultFullHttpResponse>>, Closeable {
    private final Converter<RtpPkt, List<NALU>> rtpNaluDecoder;
    private final Converter<RtpPkt, List<ADTS>> rtpAdtsDecoder;
    private final Mono<DefaultFullHttpResponse> empty = Mono.empty();
    private final ContainerSplitter splitter;
    private static final byte[] aud = new byte[]{0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xf0};
    private long lastVideoTimeStamp = 0;

    RtpH264AacHandler(ContainerSplitter splitter) {
        this.splitter = splitter;
        this.rtpNaluDecoder = new RtpNALUDecoder(this.splitter.getSdpElementParser().getVideoElementSpecific());
        this.rtpAdtsDecoder = new RtpADTSDecoder(this.splitter.getSdpElementParser().getAudioElementSpecific());
    }

    @Override
    public Mono<DefaultFullHttpResponse> apply(RtpPkt rtpPkt, RtspSession session) {
        InterLeavedRTPSession rtpSession = session.getRTPSessions()[session.getStreamIndex(rtpPkt.getRtpChannle())];
        if (rtpPkt.getRtpChannle() == rtpSession.rtpChannel()) {
            if (rtpSession.getMediaStream().getMediaType() == MediaType.VIDEO) {
                if (lastVideoTimeStamp == 0) {
                    addSpsPps(rtpPkt.getTimeStamp());
                }
                if (lastVideoTimeStamp != rtpPkt.getTimeStamp() && lastVideoTimeStamp != 0) {
                    splitter.split(rtpSession.getMediaStream().getMediaType(), rtpPkt.getTimeStamp(), aud);
                }
                List<NALU> naluList = rtpNaluDecoder.convert(rtpPkt);
                callSplitter(rtpPkt.getTimeStamp(), rtpSession.getMediaStream().getMediaType(), naluList);
                this.lastVideoTimeStamp = rtpPkt.getTimeStamp();
            }
            if (rtpSession.getMediaStream().getMediaType() == MediaType.AUDIO) {
                List<ADTS> adtsList = rtpAdtsDecoder.convert(rtpPkt);
                callSplitter(rtpPkt.getTimeStamp(), rtpSession.getMediaStream().getMediaType(), adtsList);
            }
        }
        return empty;
    }

    private <T extends Raw> void callSplitter(long timeStamp, MediaType mediaType, List<T> naluList) {
        splitter.split(mediaType, timeStamp, naluList);
    }

    private void addSpsPps(long timeStamp) {
        SdpUtil.getSpropParameter(this.splitter.getSdpElementParser().getSessionDescription())
                .stream().map(its -> Arrays.asList(its.split(",")))
                .filter(it -> it.size() == 2)
                .forEach(it -> {
                    ElementSpecific videoElementSpecific = splitter.getSdpElementParser().getVideoElementSpecific();
                    List<NALU> nalus = List.of(toNalu(it.get(0), videoElementSpecific), toNalu(it.get(1), videoElementSpecific));
                    callSplitter(timeStamp, MediaType.VIDEO, nalus);
                });
    }

    @Override
    public void close() {
        try {
            this.splitter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

