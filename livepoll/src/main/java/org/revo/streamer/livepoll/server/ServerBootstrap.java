package org.revo.streamer.livepoll.server;

import io.netty.handler.codec.rtsp.RtspEncoder;
import org.revo.streamer.livepoll.service.HolderImpl;
import org.revo.streamer.livepoll.server.rtspHandler.RtspRtpHandler;
import org.revo.streamer.livepoll.codec.rtsp.RtspRequestDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Configuration
public class ServerBootstrap {
    @Bean
    public DisposableServer tcpServer(HolderImpl holder, @Value("${server.port:8080}") Integer port) {
        return TcpServer.create().port(port + 1)
                .doOnConnection(it -> it.addHandlerLast(new RtspEncoder()).addHandlerLast(new RtspRequestDecoder()))
                .handle((inbound, outbound) -> {
                    RtspRtpHandler handler = new RtspRtpHandler(holder);
                    return outbound.sendObject(inbound.receiveObject().doOnCancel(handler::close).doOnTerminate(handler::close).flatMap(handler));
                }).bindNow();
    }
}
