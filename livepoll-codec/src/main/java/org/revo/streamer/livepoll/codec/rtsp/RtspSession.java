package org.revo.streamer.livepoll.codec.rtsp;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.revo.streamer.livepoll.codec.commons.rtp.d.InterLeavedRTPSession;
import org.revo.streamer.livepoll.codec.commons.rtp.d.MediaStream;
import org.revo.streamer.livepoll.codec.commons.utils.URLObject;
import org.revo.streamer.livepoll.codec.sdp.SdpUtil;

import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SessionDescription;
import javax.sip.TransportNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
public class RtspSession {
    private final String id;
    private final String uri;
    private String streamId;
    private InterLeavedRTPSession[] rtpSessions = null;
    private SessionDescriptionImpl sd;
    private String sdp;
    private Date createdDate;

    public RtspSession(String uri) {
        this.uri = uri;
        this.id = UUID.randomUUID().toString();
    }

    public static RtspSession from(DefaultFullHttpRequest request) {
        return new RtspSession(request.uri())
                .withSdp(request.content().toString(StandardCharsets.UTF_8))
                .setStreamId(URLObject.getId(request.uri()))
                .setCreatedDate(new Date());
    }

    public RtspSession setStreamId(String streamId) {
        this.streamId = streamId;
        return this;
    }

    public RtspSession setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public int getStreamIndex(int channel) {
        for (int i = 0; i < rtpSessions.length; i++) {
            if (null == rtpSessions[i]) {
                continue;
            }

            if (rtpSessions[i].getRtcpChannel() == channel || rtpSessions[i].getRtpChannel() == channel) {
                return i;
            }
        }

        return -1;
    }

    public Transport setup(String url, Transport transport) throws TransportNotSupportedException {
        if (!StringUtils.equals(Transport.RTP_AVP_TCP, transport.getTranport())) {
            throw new TransportNotSupportedException(transport.getTranport());
        }

        if (!StringUtils.equals(Transport.UNICAST, transport.getUnicast())) {
            throw new TransportNotSupportedException(transport.getUnicast());
        }

        int[] interleaved = transport.getInterleaved();
        if (null == interleaved) {
            throw new TransportNotSupportedException("interleaved");
        }

        String uri = URLObject.getUri(url);
//        /aslive/live/test/streamid=0             rtsp://127.0.0.1:8085/aslive/live/test/streamid=0
//        /aslive/live/test                        rtsp://127.0.0.1:8085/aslive/live/test?l=10/streamid=0
        int mediaIndex = 0;
        for (MediaDescription dm : getMediaDescriptions(sd)) {
            try {
                if (StringUtils.endsWith(uri, getControlUri(dm))) {
                    MediaStream stream = new MediaStream(mediaIndex, dm, url);
                    rtpSessions[mediaIndex] = new InterLeavedRTPSession(stream, interleaved[0], interleaved[1]);
                    return transport;
                }
            } catch (IllegalArgumentException ex) {
//                logger.warn("{}", ex.getMessage(), ex);
            }

            mediaIndex++;
        }

        throw new IllegalArgumentException("No Media Found in SDP, Named as '" + uri + "'");
    }


    public RtspSession withSdp(String sdp) {
        this.sdp = sdp;
        this.sd = SdpUtil.withSdp(sdp);
        List<MediaDescription> mediaDescriptors = getMediaDescriptions(this.sd);
        this.rtpSessions = new InterLeavedRTPSession[mediaDescriptors.size()];
        return this;
    }

    private List<MediaDescription> getMediaDescriptions(SessionDescription sd) {
        try {
            if (null != sd) {
                @SuppressWarnings("rawtypes")
                Vector vector = sd.getMediaDescriptions(false);
                if (null != vector) {
                    return new ArrayList<MediaDescription>(vector);
                }
            }
        } catch (SdpException e) {
//            logger.warn("fail get getMediaDescriptions from {}, {}", uri, e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private String getControlUri(MediaDescription dm) throws IllegalArgumentException {
        try {
            return URLObject.getUri(dm.getAttribute("control"));
        } catch (Exception c) {
            throw new IllegalArgumentException(c.getMessage(), c);
        }
    }

    public SessionDescription getSessionDescription() {
        return sd;
    }
}
