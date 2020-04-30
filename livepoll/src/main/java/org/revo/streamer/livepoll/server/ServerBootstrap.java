package org.revo.streamer.livepoll.server;

import io.netty.handler.codec.rtsp.RtspEncoder;
import org.reactivestreams.Publisher;
import org.revo.streamer.livepoll.codec.rtsp.RtspRequestDecoder;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.revo.streamer.livepoll.server.rtspHandler.RtspRtpHandler;
import org.revo.streamer.livepoll.service.HolderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Configuration
public class ServerBootstrap {

    @Autowired
    private HolderImpl holder;

    private BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> handler() {
        return (BiFunction<NettyInbound, NettyOutbound, Publisher<Void>>) (inbound, outbound) -> {
            RtspRtpHandler handler = new RtspRtpHandler(holder);
            return outbound.sendObject(inbound.receiveObject().doOnCancel(handler::close).doOnTerminate(handler::close).flatMap(handler));
        };
    }

    @Bean
    public DisposableServer tcpServer(@Value("${server.port:8080}") Integer port) {
        return TcpServer.create().port(port + 1)
                .doOnConnection(it -> it.addHandlerLast(new RtspEncoder()).addHandlerLast(new RtspRequestDecoder()))
                .handle(handler()).bindNow();
    }

}
