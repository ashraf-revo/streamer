package org.revo.streamer.livepoll.server;

import gov.nist.javax.sdp.parser.AttributeFieldParser;
import gov.nist.javax.sdp.parser.BandwidthFieldParser;
import gov.nist.javax.sdp.parser.ConnectionFieldParser;
import gov.nist.javax.sdp.parser.EmailFieldParser;
import gov.nist.javax.sdp.parser.InformationFieldParser;
import gov.nist.javax.sdp.parser.KeyFieldParser;
import gov.nist.javax.sdp.parser.MediaFieldParser;
import gov.nist.javax.sdp.parser.OriginFieldParser;
import gov.nist.javax.sdp.parser.PhoneFieldParser;
import gov.nist.javax.sdp.parser.ProtoVersionFieldParser;
import gov.nist.javax.sdp.parser.RepeatFieldParser;
import gov.nist.javax.sdp.parser.SessionNameFieldParser;
import gov.nist.javax.sdp.parser.TimeFieldParser;
import gov.nist.javax.sdp.parser.URIFieldParser;
import gov.nist.javax.sdp.parser.ZoneFieldParser;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.ClassPathResource;

import java.util.stream.Stream;

public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerResource(new ClassPathResource("/static/index.html"));
        // Register method for reflection
        hints.reflection().registerType(AttributeFieldParser.class);
        Stream.of(AttributeFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(BandwidthFieldParser.class);
        Stream.of(BandwidthFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(ConnectionFieldParser.class);
        Stream.of(ConnectionFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(EmailFieldParser.class);
        Stream.of(EmailFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(InformationFieldParser.class);
        Stream.of(InformationFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(KeyFieldParser.class);
        Stream.of(KeyFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(MediaFieldParser.class);
        Stream.of(MediaFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(OriginFieldParser.class);
        Stream.of(OriginFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(PhoneFieldParser.class);
        Stream.of(PhoneFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(ProtoVersionFieldParser.class);
        Stream.of(ProtoVersionFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(RepeatFieldParser.class);
        Stream.of(RepeatFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(SessionNameFieldParser.class);
        Stream.of(SessionNameFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(TimeFieldParser.class);
        Stream.of(TimeFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(URIFieldParser.class);
        Stream.of(URIFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));
        hints.reflection().registerType(ZoneFieldParser.class);
        Stream.of(ZoneFieldParser.class.getConstructors())
                .forEach(it -> hints.reflection().registerConstructor(it, ExecutableMode.INVOKE));

        // Register resources
    }

}
