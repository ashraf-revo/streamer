package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.rtsp.RtspMethods;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.FfmpegM3u8ContainerSplitter;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.revo.streamer.livepoll.codec.rtsp.action.*;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.revo.streamer.livepoll.service.HolderImpl;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Getter
public class RtspHandler implements Function<DefaultFullHttpRequest, Mono<DefaultFullHttpResponse>> {
    private final Mono<DefaultFullHttpResponse> error = Mono.error(RuntimeException::new);
    private final HolderImpl holderImpl;
    private RtpH264AacHandler rtpH264AacHandler;
    private HttpMethod state;
    private RtspSession session;

    RtspHandler(HolderImpl holderImpl) {
        this.holderImpl = holderImpl;
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
            return this.error;
    }

    private Mono<?> initSession(DefaultFullHttpRequest request) {
        if (request.method() == RtspMethods.ANNOUNCE) {
            this.session = RtspSession.from(request);
            log.info(this.session.getSdp());
            SdpElementParser parse = SdpElementParser.parse(this.session.getSessionDescription());
            if (SdpElementParser.validate(parse)) {
                FfmpegM3u8ContainerSplitter splitter = new FfmpegM3u8ContainerSplitter(parse, this.session.getStreamId());
                this.rtpH264AacHandler = new RtpH264AacHandler(splitter, this.session);
            } else {
                return this.error;
            }
        }
        return Mono.just(true);
    }

    public void close() {
        if (this.session != null && this.session.getId() != null) {
            this.holderImpl.getSessions().remove(this.session.getId());
        }
        rtpH264AacHandler.close();
    }

    public Publisher<?> applyRtp(RtpPkt rtpPkt) {
        return this.rtpH264AacHandler.apply(rtpPkt);
    }
}
