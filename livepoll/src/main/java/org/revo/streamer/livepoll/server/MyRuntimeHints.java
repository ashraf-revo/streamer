package org.revo.streamer.livepoll.server;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.stream.Stream;

public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection
        hints.reflection().registerType(gov.nist.javax.sdp.parser.AttributeFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.AttributeFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.BandwidthFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.BandwidthFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.ConnectionFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.ConnectionFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.EmailFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.EmailFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.InformationFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.InformationFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.KeyFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.KeyFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.MediaFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.MediaFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.OriginFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.OriginFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.PhoneFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.PhoneFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.ProtoVersionFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.ProtoVersionFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.RepeatFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.RepeatFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.SessionNameFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.SessionNameFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.TimeFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.TimeFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.URIFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.URIFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(gov.nist.javax.sdp.parser.ZoneFieldParser.class);
        Stream.of(gov.nist.javax.sdp.parser.ZoneFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));

        // Register resources
    }

}
