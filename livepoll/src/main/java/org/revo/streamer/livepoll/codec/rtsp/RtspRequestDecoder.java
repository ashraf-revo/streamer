package org.revo.streamer.livepoll.codec.rtsp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspVersions;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.AppendableCharSequence;
import org.revo.streamer.livepoll.codec.commons.rtp.base.RtpPkt;

import java.util.List;

public class RtspRequestDecoder extends ByteToMessageDecoder {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RtspRequestDecoder.class);

    enum STATE {
        READ_FIRST_BYTE,
        READ_INITAL,
        READ_RTP_CHANNEL,
        READ_RTP_LENGTH,
        READ_RTP,

        SKIP_CONTROL_CHARS,
        READ_INITIAL,
        READ_HEADER,
        READ_VARIABLE_LENGTH_CONTENT,
        BAD_MESSAGE,
        UPGRADED
    }

    private int remains = 1;
    private STATE state = STATE.READ_FIRST_BYTE;

    private int rtpChannle;
    private int rtpLength;

    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;
    private final boolean validateHeaders;
    private final AppendableCharSequence seq = new AppendableCharSequence(128);

    private HttpMessage message;
    private long contentLength = Long.MIN_VALUE;

    public RtspRequestDecoder() {
        this(4096, 8192, 8192, true);
    }

    /**
     * Creates a new instance with the specified parameters.
     */
    private RtspRequestDecoder(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize,
            boolean validateHeaders) {
        if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException(
                    "maxInitialLineLength must be a positive integer: " +
                            maxInitialLineLength);
        }
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException(
                    "maxHeaderSize must be a positive integer: " +
                            maxHeaderSize);
        }
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException(
                    "maxChunkSize must be a positive integer: " +
                            maxChunkSize);
        }
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
        this.maxChunkSize = maxChunkSize;
        this.validateHeaders = validateHeaders;
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        // 释放缓存
        if (null != message) {
            ReferenceCountUtil.release(message);
            message = null;
        }

        // 通知其他 handler
        super.handlerRemoved0(ctx);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= remains) {
            switch (state()) {
                case SKIP_CONTROL_CHARS:
                    skipControlCharacters(in);
                    state(STATE.READ_FIRST_BYTE, 1);
                    break;
                case READ_FIRST_BYTE:
                    int ch = in.readByte();
                    if (ch == '$') {
                        state(STATE.READ_RTP_CHANNEL, 1);
                    } else {
                        in.readerIndex(in.readerIndex() - 1);
                        state(STATE.READ_INITIAL, 2);
                        seq.reset();
                    }
                    break;
                case READ_RTP_CHANNEL:
                    rtpChannle = in.readUnsignedByte();

                    state(STATE.READ_RTP_LENGTH, 2);
                    break;
                case READ_RTP_LENGTH:
                    rtpLength = in.readUnsignedShort();

                    state(STATE.READ_RTP, rtpLength);
                    break;
                case READ_RTP:

                    // no copy
                    ByteBuf rtpContent = in.slice(in.readerIndex(), rtpLength).retain();
                    in.skipBytes(rtpLength);
                    byte[] data = new byte[rtpContent.readableBytes()];
                    rtpContent.readBytes(data);
                    rtpContent.release();
                    out.add(new RtpPkt(rtpChannle, data));
                    state(STATE.READ_FIRST_BYTE, remains);
                    break;
                case READ_INITIAL:
                    char ch1 = (char) in.readByte();
                    char ch2 = (char) in.readByte();
                    if (ch2 == HttpConstants.LF) {
                        if (ch1 != HttpConstants.CR) {
                            seq.append(ch1);
                        }

                        logger.debug("{} {}", state(), seq);
                        String[] lines = splitInitialLine(seq);
                        message = createMessage(lines);

                        seq.reset();
                        state(STATE.READ_HEADER, 2);
                    } else if (seq.length() < maxInitialLineLength) {
                        seq.append(ch1);
                        in.readerIndex(in.readerIndex() - 1);
                    } else {
                        message = createInvalidMessage();
                        out.add(message);
                        message = null;
                        state(STATE.SKIP_CONTROL_CHARS, Math.max(1, in.readableBytes()));
                    }

                    break;
                case READ_HEADER:
                    ch1 = (char) in.readByte();
                    ch2 = (char) in.readByte();

                    if (ch2 == HttpConstants.LF) {
                        if (ch1 != HttpConstants.CR) {
                            seq.append(ch1);
                        }

                        logger.debug("{} {}", state(), seq);
                        if (seq.length() > 0) {
                            String[] headers = splitHeader(seq);
                            message.headers().add(headers[0], headers[1]);
                            seq.reset();
                            state(STATE.READ_HEADER, 2);
                        } else {
                            long contentLength = contentLength();
                            if (contentLength < 0) {
                                contentLength = 0;
                            }

                            if (contentLength > maxChunkSize) {
                                throw new IllegalArgumentException("content length is too large");
                            }
                            state(STATE.READ_VARIABLE_LENGTH_CONTENT, (int) contentLength);
                        }

                    } else if (seq.length() < maxHeaderSize) {
                        seq.append(ch1);
                        in.readerIndex(in.readerIndex() - 1);
                    } else {
                        message = createInvalidMessage();
                        out.add(message);
                        message = null;
                        state(STATE.SKIP_CONTROL_CHARS, Math.max(1, in.readableBytes()));
                    }

                    break;
                case READ_VARIABLE_LENGTH_CONTENT:
                    ByteBuf bytes = LastHttpContent.EMPTY_LAST_CONTENT.content();
                    if (remains > 0) {
                        bytes = in.readBytes(remains);
                    }

                    if (message instanceof HttpRequest) {
                        HttpRequest request = (HttpRequest) message;
                        DefaultFullHttpRequest full = new DefaultFullHttpRequest(request.protocolVersion(), request.method(), request.uri(), bytes, validateHeaders);
                        full.headers().add(request.headers());
                        out.add(full);
                        message = null;
                    } else if (message instanceof HttpResponse) {
                        HttpResponse response = (HttpResponse) message;
                        DefaultFullHttpResponse full = new DefaultFullHttpResponse(response.protocolVersion(), response.status(), bytes, validateHeaders);
                        full.headers().add(response.headers());
                        out.add(full);
                        message = null;
                    }
                    state(STATE.SKIP_CONTROL_CHARS, 1);
                    break;
                default:
                    throw new IllegalStateException("unsupported state = " + state());
            }
        }

    }

    private HttpMessage createMessage(String[] initialLine) {
        return new DefaultHttpRequest(RtspVersions.valueOf(initialLine[2]),
                RtspMethods.valueOf(initialLine[0]), initialLine[1], validateHeaders);
    }

    private HttpMessage createInvalidMessage() {
        return new DefaultFullHttpRequest(
                RtspVersions.RTSP_1_0, RtspMethods.OPTIONS, "/bad-request", Unpooled.EMPTY_BUFFER, validateHeaders);
    }

    private long contentLength() {
        contentLength = HttpUtil.getContentLength(message, -1);
        return contentLength;
    }

    private static void skipControlCharacters(ByteBuf buffer) {
        for (; ; ) {
            char c = (char) buffer.readUnsignedByte();
            if (!Character.isISOControl(c) &&
                    !Character.isWhitespace(c)) {
                buffer.readerIndex(buffer.readerIndex() - 1);
                break;
            }
        }
    }


    private STATE state() {
        return state;
    }

    private void state(STATE newState, int remains) {
        this.state = newState;
        this.remains = remains;
    }


    private static String[] splitInitialLine(AppendableCharSequence sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);

        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);

        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[]{
                sb.substring(aStart, aEnd),
                sb.substring(bStart, bEnd),
                cStart < cEnd ? sb.substring(cStart, cEnd) : ""};
    }

    private static String[] splitHeader(AppendableCharSequence sb) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;

        nameStart = findNonWhitespace(sb, 0);
        for (nameEnd = nameStart; nameEnd < length; nameEnd++) {
            char ch = sb.charAt(nameEnd);
            if (ch == ':' || Character.isWhitespace(ch)) {
                break;
            }
        }

        for (colonEnd = nameEnd; colonEnd < length; colonEnd++) {
            if (sb.charAt(colonEnd) == ':') {
                colonEnd++;
                break;
            }
        }

        valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            return new String[]{
                    sb.substring(nameStart, nameEnd),
                    ""
            };
        }

        valueEnd = findEndOfString(sb);
        return new String[]{
                sb.substring(nameStart, nameEnd),
                sb.substring(valueStart, valueEnd)
        };
    }

    private static int findNonWhitespace(CharSequence sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private static int findWhitespace(CharSequence sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private static int findEndOfString(CharSequence sb) {
        int result;
        for (result = sb.length(); result > 0; result--) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }

}
