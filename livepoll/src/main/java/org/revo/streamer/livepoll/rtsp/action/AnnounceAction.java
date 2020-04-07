package org.revo.streamer.livepoll.rtsp.action;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspVersions;
import io.netty.util.AsciiString;
import org.revo.streamer.livepoll.rtsp.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;

import static org.revo.streamer.livepoll.rtsp.utils.MessageUtils.append;
import static org.revo.streamer.livepoll.rtsp.utils.MessageUtils.get;


public class AnnounceAction extends BaseAction<DefaultFullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(AnnounceAction.class);

    public AnnounceAction(DefaultFullHttpRequest req, RtspSession rtspSession) {
        super(req, rtspSession);
    }


    @Override
    public DefaultFullHttpResponse call() {
        DefaultFullHttpResponse rep = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
        get(req, RtspHeaderNames.CSEQ).ifPresent(it -> append(rep, it));
        get(req, RtspHeaderNames.SESSION).ifPresent(it -> append(rep, it));
        Optional<SimpleImmutableEntry<AsciiString, String>> content_type = get(req, RtspHeaderNames.CONTENT_TYPE);
        if (!content_type.isPresent() || !content_type.get().getValue().contains("application/sdp")) {
            return new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        return rep;
    }

}
