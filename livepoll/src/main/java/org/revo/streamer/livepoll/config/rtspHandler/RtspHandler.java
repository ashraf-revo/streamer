package org.revo.streamer.livepoll.config.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.revo.streamer.livepoll.Services.HolderImpl;
import org.revo.streamer.livepoll.commons.container.m3u8.M3u8Splitter;
import org.revo.streamer.livepoll.rtsp.RtspSession;
import org.revo.streamer.livepoll.rtsp.action.*;
import org.revo.streamer.livepoll.util.SdpElementParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

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
            return initSession(request).map(it -> new AnnounceAction(request, this.session).call());
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
                M3u8Splitter m3u8Splitter = new M3u8Splitter(2, this.session.getStreamId(),
                        this.holderImpl.getFileStorage(), parse, (var1, var2, var3) -> System.out.println(var1 + "  " + var3 + " time " + var2));
                this.rtpHandler = new RtpHandler(m3u8Splitter);
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
}
