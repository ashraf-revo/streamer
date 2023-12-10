package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.reactivestreams.Publisher;
import org.revo.streamer.livepoll.codec.commons.container.Mp4ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.revo.streamer.livepoll.codec.rtsp.action.*;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.revo.streamer.livepoll.service.HolderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class RtspHandler implements Function<DefaultFullHttpRequest, Mono<DefaultFullHttpResponse>> {
    private RtpH264AacHandler rtpH264AacHandler;
    private HttpMethod state;
    private RtspSession session;
    private final Mono<DefaultFullHttpResponse> error = Mono.error(RuntimeException::new);
    private final HolderImpl holderImpl;
    private final Logger logger = LoggerFactory.getLogger(RtspHandler.class);

    RtspHandler(HolderImpl holderImpl) {
        this.holderImpl = holderImpl;
    }

    RtpH264AacHandler getRtpHandler() {
        return rtpH264AacHandler;
    }

    @Override
    public Mono<DefaultFullHttpResponse> apply(DefaultFullHttpRequest request) {
        if (request.method() == RtspMethods.OPTIONS) {
            this.state = RtspMethods.OPTIONS;
            return Mono.just(new OptionsAction(request, this.session).call());
        } else if (this.state == RtspMethods.OPTIONS && request.method() == RtspMethods.ANNOUNCE) {
            this.state = RtspMethods.ANNOUNCE;
            return initSession(request)
                    .doOnNext(it -> this.holderImpl.getSessions().put(this.session.getId(), this.session))
                    .map(it -> new AnnounceAction(request, this.session).call());
        } else if ((this.state == RtspMethods.ANNOUNCE || this.state == RtspMethods.SETUP) && request.method() == RtspMethods.SETUP) {
            this.state = RtspMethods.SETUP;
            return Mono.just(new SetupAction(request, this.session).call());
        } else if (this.state == RtspMethods.SETUP && request.method() == RtspMethods.RECORD) {
            this.state = RtspMethods.RECORD;
            return Mono.just(new RecordAction(request, this.session).call());
        } else if (request.method() == RtspMethods.TEARDOWN) {
            this.state = RtspMethods.TEARDOWN;
            return Mono.just(new TeardownAction(request, this.session).call());
        } else
            return error;
    }

    private Mono<?> initSession(DefaultFullHttpRequest request) {
        if (request.method() == RtspMethods.ANNOUNCE) {
            this.session = RtspSession.from(request);
            logger.info(this.session.getSdp());
            SdpElementParser parse = SdpElementParser.parse(this.session.getSessionDescription());
            if (SdpElementParser.validate(parse)) {
                Mp4ContainerSplitter splitter = new Mp4ContainerSplitter(parse, this.session.getStreamId());
                this.rtpH264AacHandler = new RtpH264AacHandler(splitter);
            } else {
                return error;
            }
        }
        return Mono.just(true);
    }

    HttpMethod getState() {
        return state;
    }

    RtspSession getSession() {
        return session;
    }

    public void close() {
        if (this.session != null && this.session.getId() != null) {
            this.holderImpl.getSessions().remove(this.session.getId());
        }
        rtpH264AacHandler.close();
    }

    public Publisher<?> applyRtp(RtpPkt rtpPkt, RtspSession session) {
        return getRtpHandler().apply(rtpPkt, session);
    }
}
