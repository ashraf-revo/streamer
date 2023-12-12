package org.revo.streamer.livepoll.codec.rtsp.action;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspVersions;
import org.revo.streamer.livepoll.codec.rtsp.RtspSession;
import org.revo.streamer.livepoll.codec.rtsp.Transport;

import javax.sip.TransportNotSupportedException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;

import static org.revo.streamer.livepoll.codec.commons.utils.MessageUtils.append;
import static org.revo.streamer.livepoll.codec.commons.utils.MessageUtils.get;


public class SetupAction extends BaseAction<DefaultFullHttpRequest> {
    public SetupAction(DefaultFullHttpRequest req, RtspSession rtspSession) {
        super(req, rtspSession);
    }

    @Override
    public DefaultFullHttpResponse call() {
        DefaultFullHttpResponse rep = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
        get(req, RtspHeaderNames.CSEQ)
                .ifPresent(it ->
                        append(rep, it));
        Optional.of(get(req, RtspHeaderNames.SESSION)
                        .orElseGet(() ->
                                new SimpleImmutableEntry<>(RtspHeaderNames.SESSION, rtspSession.getId())))
                .ifPresent(it ->
                        append(rep, it));
        get(req, RtspHeaderNames.TRANSPORT)
                .map(it -> new SimpleImmutableEntry<>(it.getKey(), Transport.parse(it.getValue())))
                .ifPresent(it -> {
                    try {
                        Transport transport = rtspSession.setup(req.uri(), it.getValue());
                        rep.headers().add(it.getKey(), transport.toString());
                    } catch (TransportNotSupportedException e) {
                        System.out.println(e.getMessage());
                    }
                });

        return rep;
    }
}
