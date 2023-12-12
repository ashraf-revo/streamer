package org.revo.streamer.livepoll.codec.commons.rtp.d;

import lombok.Getter;

import java.util.Random;

@Getter
public class InterLeavedRTPSession {
    @Getter
    private final MediaStream mediaStream;
    private final int rtpChannel;
    private final int rtcpChannel;
    private String cname = null;
    private long ssrc;
    private Random random = null;

    public InterLeavedRTPSession(MediaStream mediaStream, int rtpChannel, int rtcpChannel) {

        this.mediaStream = mediaStream;
        this.rtpChannel = rtpChannel;
        this.rtcpChannel = rtcpChannel;
        this.generateCNAME();
        this.generateSsrc();
    }

    private void generateSsrc() {
        if (this.random == null)
            createRandom();

        // Set an SSRC
        this.ssrc = this.random.nextInt();
        if (this.ssrc < 0) {
            this.ssrc = this.ssrc * -1;
        }
    }

    private void createRandom() {
        this.random = new Random(System.currentTimeMillis() + Thread.currentThread().getId()
                - Thread.currentThread().hashCode() + this.cname.hashCode());
    }

    private void generateCNAME() {
        cname = System.getProperty("user.name") + "@" + System.getenv("HOSTNAME");
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{RtpSession");
        buf.append(", ").append(mediaStream.getMediaType());
        buf.append("}");
        return buf.toString();
    }

}
