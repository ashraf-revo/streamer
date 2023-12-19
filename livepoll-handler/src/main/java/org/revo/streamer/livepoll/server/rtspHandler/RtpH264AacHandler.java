package org.revo.streamer.livepoll.server.rtspHandler;

import lombok.SneakyThrows;
import org.revo.streamer.livepoll.codec.commons.container.StreamContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.Converter;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpADTSDecoder;
import org.revo.streamer.livepoll.codec.commons.rtp.RtpNALUDecoder;
import org.revo.streamer.livepoll.codec.commons.rtp.base.ADTS;
import org.revo.streamer.livepoll.codec.commons.rtp.base.NALU;
import org.revo.streamer.livepoll.codec.commons.rtp.base.Packet;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.commons.rtp.d.InterLeavedRTPSession;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaType;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.List;
import java.util.function.Function;

public class RtpH264AacHandler implements Function<RtpPkt, Mono<Void>>, Closeable {
    private final Converter<RtpPkt, List<NALU>> rtpNaluDecoder;
    private final Converter<RtpPkt, List<ADTS>> rtpAdtsDecoder;
    private final Mono<Void> empty = Mono.empty();
    private final StreamContainerSplitter splitter;
    private final RtspSession session;

    public RtpH264AacHandler(StreamContainerSplitter splitter, RtspSession session) {
        this.splitter = splitter;
        this.session = session;
        this.rtpNaluDecoder = new RtpNALUDecoder(this.splitter.getSdpElementParser().getVideoElementSpecific());
        this.rtpAdtsDecoder = new RtpADTSDecoder(this.splitter.getSdpElementParser().getAudioElementSpecific());
    }

    @Override
    public Mono<Void> apply(RtpPkt rtpPkt) {
        InterLeavedRTPSession rtpSession = session.getRtpSessions()[session.getStreamIndex(rtpPkt.getRtpChannle())];
        if (rtpPkt.getRtpChannle() == rtpSession.getRtpChannel()) {
            if (rtpSession.getMediaStream().getMediaType() == MediaType.VIDEO) {
                callSplitter(rtpPkt.getTimeStamp(), rtpSession.getMediaStream().getMediaType(), rtpNaluDecoder.convert(rtpPkt));
            }
            if (rtpSession.getMediaStream().getMediaType() == MediaType.AUDIO) {
                callSplitter(rtpPkt.getTimeStamp(), rtpSession.getMediaStream().getMediaType(), rtpAdtsDecoder.convert(rtpPkt));
            }
        }
        return empty;
    }

    private <T extends Packet> void callSplitter(long timeStamp, MediaType mediaType, List<T> naluList) {
        splitter.split(mediaType, timeStamp, naluList);
    }

    @SneakyThrows
    @Override
    public void close() {
        this.splitter.close();
    }

}

