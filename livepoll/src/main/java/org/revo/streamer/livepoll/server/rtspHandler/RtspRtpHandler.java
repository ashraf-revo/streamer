package org.revo.streamer.livepoll.server.rtspHandler;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.rtsp.RtspMethods;
import org.reactivestreams.Publisher;
import org.revo.streamer.livepoll.service.HolderImpl;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;

import java.util.function.Function;


public class RtspRtpHandler implements Function<Object, Publisher<?>> {
    private RtspHandler rtspHandler;


    public RtspRtpHandler(HolderImpl holderImpl) {
        this.rtspHandler = new RtspHandler(holderImpl);
    }

    @Override
    public Publisher<?> apply(Object o) {
        if (o instanceof DefaultFullHttpRequest) {
            return rtspHandler.apply(((DefaultFullHttpRequest) o));
        } else if (o instanceof RtpPkt && this.rtspHandler.getState() == RtspMethods.RECORD) {
            return rtspHandler.getRtpHandler().apply((RtpPkt) o, this.rtspHandler.getSession());
        } else //       close(signal, "not follwing rtsp seqance (OPTIONS,ANNOUNCE,SETUP,RECORD,TEARDOWN)");
            return rtspHandler.error;
    }

    public void close() {
        rtspHandler.close();
    }
}
