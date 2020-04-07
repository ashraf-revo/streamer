package org.revo.streamer.livepoll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LivepollApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivepollApplication.class, args);
    }
/*
    @Bean
    public FluxProcessor<Message<byte[]>, Message<byte[]>> processor() {
        return UnicastProcessor.create();
//        return ReplayProcessor.create();
//        return ReplayProcessor.create(0);
//        return DirectProcessor.create();
//        return TopicProcessor.create();
// i need it no cache , i don,t need it to buffer any thing , i need it to be live stream
//        return ReplayProcessor.createTimeout(Duration.ofMillis(1000));
    }


    @Bean
    public Flux<Message<byte[]>> stream(FluxProcessor<Message<byte[]>, Message<byte[]>> processor) {
        return processor.publish().autoConnect();
    }

    @StreamListener(PiSink.INPUT2)
    public synchronized void handleSdp(Message<byte[]> message) {
        NALU.NaluHeader naluHeader = NALU.NaluHeader.read(message.getPayload()[0]);
        if (naluHeader.getTYPE() == 7) {
            streamService.setSps(message.getHeaders().get("streamId").toString(), message.getPayload());
        }
        if (naluHeader.getTYPE() == 8) {
            streamService.setPps(message.getHeaders().get("streamId").toString(), message.getPayload());
        }
    }

    @Bean
    public RouterFunction<ServerResponse> function(AuthService authService, Flux<Message<byte[]>> stream) {
        return route(GET("/video/{id}"), serverRequest -> ok()
                .header("Content-Type", "video/h264")
                .body(Flux.fromIterable(streamService.findOneById(serverRequest.pathVariable("id")).
                                map(it -> Arrays.asList(it.getVideoContent().getSps(), it.getVideoContent().getPps()))
                                .orElse(Collections.emptyList()))
                                .mergeWith(stream.filter(it -> VIDEO.name().equals(it.getHeaders().get("type").toString()))
                                        .filter(it -> Objects.equals(it.getHeaders().get("streamId").toString(), serverRequest.pathVariable("id")) && it.getPayload().length > 0)
                                        .map(Message::getPayload))
                                .map(NALU::getRaw)
                                .map(ByteBuffer::wrap)
                        , ByteBuffer.class)).andRoute(GET("/audio/{id}"), serverRequest -> ok()
                .header("Content-Type", "audio/aac")
                .body(stream.filter(it -> AUDIO.name().equals(it.getHeaders().get("type").toString()))
                                .filter(it -> Objects.equals(it.getHeaders().get("streamId").toString(), serverRequest.pathVariable("id")) && it.getPayload().length > 0)
                                .map(Message::getPayload)
                                .map(AdtsFrame::getRaw)
                                .map(ByteBuffer::wrap)
                        , ByteBuffer.class))
                .andRoute(GET("/user"), serverRequest -> ok().body(authService.currentJwtUserId()
                        .map(it -> "user " + it + "  from " + serverRequest.exchange().getRequest().getRemoteAddress()), String.class));
    }

*/

}
