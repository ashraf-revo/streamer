package org.revo.streamer.livepoll.codec.commons.rtp.base;

import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import static org.revo.streamer.livepoll.codec.sdp.AudioClockRate.getByFrequency;

public class ADTS extends Packet {
    private static final byte[] ADTS_HEADER = new byte[]{(byte) 0xFF, (byte) 0xF1, (byte) 0x40, (byte) 0x80, (byte) 0x2F, (byte) 0x5F, (byte) 0xFC};
    private final byte[] raw;

    // https://wiki.multimedia.cx/index.php/ADTS
    //FF F1 5C 80 00 0F FC 21
//    FF F1 4C 80 00 1F FC
    public ADTS(byte[] payload, int offset, int size, ElementSpecific specific) {
        byte[] header = buildHeader(size, specific);
        raw = new byte[size + header.length];
        System.arraycopy(header, 0, raw, 0, header.length);
        System.arraycopy(payload, offset, raw, header.length, size);
    }

    private static byte[] buildHeader(int size, ElementSpecific specific) {
        byte[] header = new byte[ADTS_HEADER.length];
        System.arraycopy(ADTS_HEADER, 0, header, 0, header.length);
        size += header.length;
        header[2] |= (byte) ((getByFrequency(specific.clockRate()).getFrequency()) << 2);
        header[3] |= (byte) ((size & 0x1800) >> 11);
        header[4] = (byte) ((size & 0x1FF8) >> 3);
        header[5] = (byte) ((size & 0x7) << 5);
        return header;
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }

    @Override
    public byte[] getPayload() {
        byte[] payload = new byte[raw.length - headerSize()];
        System.arraycopy(raw, headerSize(), payload, 0, payload.length);
        return payload;
    }

    @Override
    public int headerSize() {
        return ADTS_HEADER.length;
    }
}
