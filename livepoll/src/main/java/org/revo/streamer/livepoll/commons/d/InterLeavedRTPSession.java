package org.revo.streamer.livepoll.commons.d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


/**
 * RTP over tcp
 *
 * @author 陈修恒
 * @date 2016年4月28日
 */
public class InterLeavedRTPSession {
    private static final Logger logger = LoggerFactory
            .getLogger(InterLeavedRTPSession.class);

    private MediaStream mediaStream;
    private int rtpChannel;
    private int rtcpChannel;

    private String cname = null;

    private long ssrc;
    private Random random = null;


    private PlayState state = PlayState.WAITING;

    public InterLeavedRTPSession(MediaStream mediaStream, int rtpChannel, int rtcpChannel) {

        this.mediaStream = mediaStream;
        this.rtpChannel = rtpChannel;
        this.rtcpChannel = rtcpChannel;
        this.generateCNAME();
        this.generateSsrc();
    }


    public void await() {
        state(PlayState.WAITING);
    }


    private PlayState state() {
        return state;
    }

    private void state(PlayState newState) {
        PlayState oldState = this.state;
        this.state = newState;

        if (oldState != newState) {
            logger.info("Playing {}, {}", newState, this);
        }
    }


    public int rtcpChannel() {
        return rtcpChannel;
    }

    public int rtpChannel() {
        return rtpChannel;
    }


    public MediaStream getMediaStream() {
        return mediaStream;
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
