package org.revo.streamer.livepoll.server;

import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ImportRuntimeHints(MyRuntimeHints.class)
public class Config {
    @Bean
    public Map<String, RtspSession> sessions() {
        return new ConcurrentHashMap<>();
    }
}
