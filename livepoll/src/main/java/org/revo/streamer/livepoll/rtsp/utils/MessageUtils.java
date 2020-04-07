package org.revo.streamer.livepoll.rtsp.utils;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;

import java.util.AbstractMap;
import java.util.Optional;

public class MessageUtils {
    public static <T extends HttpRequest> Optional<AbstractMap.SimpleImmutableEntry<AsciiString, String>> get(T t, AsciiString asciiString) {
        return Optional.ofNullable(t.headers().get(asciiString))
                .map(it -> new AbstractMap.SimpleImmutableEntry<>(asciiString, it));
    }

    public static <T extends HttpRequest> Optional<AbstractMap.SimpleImmutableEntry<AsciiString, String>> set(AsciiString asciiString, String defaults) {
        return Optional.ofNullable(defaults)
                .map(it -> new AbstractMap.SimpleImmutableEntry<>(asciiString, it));
    }

    public static void append(DefaultFullHttpResponse rep, AbstractMap.SimpleImmutableEntry<AsciiString, String> entry) {
        rep.headers().add(entry.getKey(), entry.getValue());
    }

}
