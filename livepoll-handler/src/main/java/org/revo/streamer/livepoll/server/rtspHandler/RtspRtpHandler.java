package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.reactivestreams.Publisher;
import org.revo.streamer.livepoll.service.HolderImpl;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;
import reactor.core.publisher.Mono;

import java.util.function.Function;


public class RtspRtpHandler implements Function<Object, Publisher<DefaultFullHttpResponse>> {
    private final RtspHandler rtspHandler;
    private final Mono<DefaultFullHttpResponse> error = Mono.error(RuntimeException::new);


    public RtspRtpHandler(HolderImpl holderImpl) {
        this.rtspHandler = new RtspHandler(holderImpl);
    }

    @Override
    public Publisher<DefaultFullHttpResponse> apply(Object o) {
        if (o instanceof DefaultFullHttpRequest request) {
            return rtspHandler.apply(request);
        } else if (o instanceof RtpPkt rtpPkt && this.rtspHandler.getState() == RtspMethods.RECORD) {
            return rtspHandler.getRtpHandler().apply(rtpPkt, this.rtspHandler.getSession());
        } else //       close(signal, "not follwing rtsp seqance (OPTIONS,ANNOUNCE,SETUP,RECORD,TEARDOWN)");
            return error;
    }

    public void close() {
        rtspHandler.close();
    }
}
