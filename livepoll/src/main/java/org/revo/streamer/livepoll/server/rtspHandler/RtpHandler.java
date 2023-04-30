package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.Decoder;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpADTSDecoder;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpNALUDecoder;
import org.revo.streamer.livepoll.codec.commons.rtp.base.ADTS;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.commons.rtp.d.InterLeavedRTPSession;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.BiFunction;

public class RtpHandler implements BiFunction<RtpPkt, RtspSession, Mono<DefaultFullHttpResponse>>, Closeable {
    private final Decoder<RtpPkt, NALU> rtpNaluDecoder;
    private final Decoder<RtpPkt, ADTS> rtpAdtsDecoder;
    private final Mono<DefaultFullHttpResponse> empty = Mono.empty();
    private final ContainerSplitter splitter;

    RtpHandler(ContainerSplitter splitter) {
        this.splitter = splitter;
        this.rtpNaluDecoder = new RtpNALUDecoder(this.splitter.getSdpElementParser().getVideoElementSpecific());
        this.rtpAdtsDecoder = new RtpADTSDecoder(this.splitter.getSdpElementParser().getAudioElementSpecific());
    }

    @Override
    public Mono<DefaultFullHttpResponse> apply(RtpPkt rtpPkt, RtspSession session) {
        InterLeavedRTPSession rtpSession = session.getRTPSessions()[session.getStreamIndex(rtpPkt.getRtpChannle())];
        if (rtpPkt.getRtpChannle() == rtpSession.rtpChannel()) {

            if (rtpSession.getMediaStream().getMediaType() == MediaType.VIDEO) {
                rtpNaluDecoder.decode(rtpPkt).forEach(it ->
                        splitter.split(rtpSession.getMediaStream().getMediaType(), rtpPkt.getTimeStamp(), it.getRaw()));
            }
            if (rtpSession.getMediaStream().getMediaType() == MediaType.AUDIO) {
                rtpAdtsDecoder.decode(rtpPkt).forEach(it ->
                        splitter.split(rtpSession.getMediaStream().getMediaType(), rtpPkt.getTimeStamp(), it.getRaw()));
            }
        }

        return empty;
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

