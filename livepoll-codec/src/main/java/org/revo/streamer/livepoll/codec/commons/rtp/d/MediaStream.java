package org.revo.streamer.livepoll.codec.commons.rtp.d;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sdp.Media;
import javax.sdp.MediaDescription;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Slf4j
public class MediaStream {
    public static final int SSRC_UNKNOWN = -1;


    private final String url;
    private final MediaDescription md;
    private final int streamIndex;

    private MediaType mediaType = MediaType.UNKNOWN;
    private String codec;
    private Rational timeUnit;
    private int channels;


    public MediaStream(int streamIndex, MediaDescription md, String url) {
        this.md = md;
        this.url = url;
        this.streamIndex = streamIndex;

        this.timeUnit = Rational.$_1_000;
        this.channels = 1;
        try {
            Media media = md.getMedia();
            if (null != media) {
                mediaType = MediaType.typeOf(media.getMediaType());
            }
            if (mediaType.isAudio()) {
                channels = 1;
                timeUnit = Rational.$_8_000;
            } else if (mediaType.isVideo()) {
                channels = 0;
                timeUnit = Rational.$90_000;
            }


            String rtpmap = md.getAttribute("rtpmap");
            if (null != rtpmap) {
                Matcher matcher = Pattern.compile("([\\d]+) ([^/]+)/([\\d]+)(/([\\d]+))?").matcher(rtpmap);
                if (matcher.find()) {
                    // payloadType = Integer.parseInt(matcher.group(1));
                    codec = matcher.group(2);
                    timeUnit = Rational.valueOf(Integer.parseInt(matcher.group(3)));

                    if (null != matcher.group(5)) {
                        this.channels = Integer.parseInt(matcher.group(5));
                    }
                }
            }

        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{RtpStream, sdp=\r\n")
                .append(md)
                .append("}");
        return buf.toString();
    }

}
