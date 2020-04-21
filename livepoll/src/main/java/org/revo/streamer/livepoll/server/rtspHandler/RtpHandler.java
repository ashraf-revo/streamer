package org.revo.streamer.livepoll.server.rtspHandler;

import org.revo.streamer.livepoll.codec.commons.rtp.d.InterLeavedRTPSession;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.commons.container.ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.Encoder;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpAdtsEncoder;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpNaluEncoder;
import org.revo.streamer.livepoll.codec.commons.rtp.base.Adts;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.BiFunction;

public class RtpHandler implements BiFunction<RtpPkt, RtspSession, Mono<?>>, Closeable {
    private Encoder<RtpPkt, NALU> rtpNaluEncoder;
    private Encoder<RtpPkt, Adts> rtpAdtsEncoder;
    private Mono<?> empty = Mono.empty();
    private ContainerSplitter splitter;

    RtpHandler(ContainerSplitter splitter) {
        this.splitter = splitter;
        this.rtpNaluEncoder = new RtpNaluEncoder(this.splitter.getSdpElementParser().getVideoElementSpecific());
        this.rtpAdtsEncoder = new RtpAdtsEncoder(this.splitter.getSdpElementParser().getAudioElementSpecific());
    }

    @Override
    public Mono<?> apply(RtpPkt rtpPkt, RtspSession session) {
        InterLeavedRTPSession rtpSession = session.getRTPSessions()[session.getStreamIndex(rtpPkt.getRtpChannle())];
        if (rtpPkt.getRtpChannle() == rtpSession.rtpChannel()) {
            if (rtpSession.getMediaStream().getMediaType() == MediaType.VIDEO) {
                rtpNaluEncoder.encode(rtpPkt).forEach(it -> {
//                    if (it.getNaluHeader().getTYPE()!=1)
                    System.out.println(it.getNaluHeader().getTYPE());
                    splitter.getVideoSlitter().split(it.getRaw());
                });
            }
            if (rtpSession.getMediaStream().getMediaType() == MediaType.AUDIO) {
                rtpAdtsEncoder.encode(rtpPkt).forEach(it -> splitter.getM3u8AudioSplitter().split(it.getRaw()));
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

