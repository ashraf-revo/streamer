package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.reactivestreams.Publisher;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import org.revo.streamer.livepoll.service.HolderImpl;
import reactor.core.publisher.Mono;

import java.util.function.Function;


public class RtspRtpHandler implements Function<Object, Publisher<?>> {
    private final RtspHandler rtspHandler;
    private final Mono<?> error = Mono.error(RuntimeException::new);


    public RtspRtpHandler(HolderImpl holderImpl) {
        this.rtspHandler = new RtspHandler(holderImpl);
    }

    @Override
    public Publisher<?> apply(Object o) {
        if (o instanceof DefaultFullHttpRequest request) {
            return this.handle(request);
        } else if (o instanceof RtpPkt rtpPkt) {
            return this.handle(rtpPkt);
        } else {
            return this.handle(error);
        }
    }

    private Publisher<?> handle(Mono<?> error) {
        return error;
    }

    private Publisher<?> handle(RtpPkt rtpPkt) {
        if (this.rtspHandler.getState() == RtspMethods.RECORD) {
            return rtspHandler.applyRtp(rtpPkt, this.rtspHandler.getSession());
        } else {
            return handle(error);
        }
    }

    private Publisher<?> handle(DefaultFullHttpRequest request) {
        return rtspHandler.apply(request);
    }

    public void close() {
        rtspHandler.close();
    }
}
