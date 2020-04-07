package org.revo.streamer.livepoll.config;

import io.netty.handler.codec.rtsp.RtspEncoder;
import org.revo.streamer.livepoll.config.rtspHandler.HolderImpl;
import org.revo.streamer.livepoll.config.rtspHandler.RtspRtpHandler;
import org.revo.streamer.livepoll.rtsp.codec.RtspRequestDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

@Configuration
public class ServerBootstrap {
    @Bean
    public DisposableServer tcpServer(HolderImpl holder, @Value("${server.port:8085}") Integer port) {
        return TcpServer.create().port(port + 1)
                .doOnConnection(it -> it.addHandlerLast(new RtspEncoder()).addHandlerLast(new RtspRequestDecoder()))
                .handle((inbound, outbound) -> outbound.sendObject(inbound.receiveObject().flatMap(new RtspRtpHandler(holder)))).bindNow();
    }
}
