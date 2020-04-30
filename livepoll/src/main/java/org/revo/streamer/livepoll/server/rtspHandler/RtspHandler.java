package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.revo.streamer.livepoll.codec.commons.container.m3u8.M3u8Splitter;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.revo.streamer.livepoll.codec.rtsp.action.*;
import org.revo.streamer.livepoll.codec.sdp.SdpElementParser;
import org.revo.streamer.livepoll.service.HolderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

public class RtspHandler implements Function<DefaultFullHttpRequest, Mono<?>> {
    private RtpHandler rtpHandler;
    private HttpMethod state;
    private RtspSession session;
    final Mono<?> error = Mono.error(RuntimeException::new);
    private HolderImpl holderImpl;
    private final Logger logger = LoggerFactory.getLogger(RtspHandler.class);

    RtspHandler(HolderImpl holderImpl) {
        this.holderImpl = holderImpl;
    }

    RtpHandler getRtpHandler() {
        return rtpHandler;
    }

    @Override
    public Mono<?> apply(DefaultFullHttpRequest request) {
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
//                    close(request, "not follwing rtsp seqance (OPTIONS,ANNOUNCE,SETUP,RECORD,TEARDOWN)");
    }

    private Mono<?> initSession(DefaultFullHttpRequest request) {
        if (request.method() == RtspMethods.ANNOUNCE) {
            this.session = RtspSession.from(request);
            logger.info(this.session.getSdp());
            SdpElementParser parse = SdpElementParser.parse(this.session.getSessionDescription());
            if (SdpElementParser.validate(parse)) {
                M3u8Splitter m3u8Splitter = null;
                try {
                    m3u8Splitter = new M3u8Splitter(1, this.session.getStreamId(),
                            this.holderImpl.getFileStorage(), parse, (var1, var2, var3) -> {
                    });
                    this.rtpHandler = new RtpHandler(m3u8Splitter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return error;
//                    close(request, "Sorry Unsupported Stream");
            }
        }
        // default return empty
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
        rtpHandler.close();
    }
}
