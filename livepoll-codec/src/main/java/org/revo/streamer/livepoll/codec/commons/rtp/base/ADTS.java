package org.revo.streamer.livepoll.codec.commons.rtp.base;

import org.revo.streamer.livepoll.codec.sdp.ElementSpecific;

import static org.revo.streamer.livepoll.codec.sdp.AudioClockRate.getByFrequency;

public class ADTS extends Packet {
    private byte[] raw;
    private static final byte[] adtsHeader = new byte[]{(byte) 0xFF, (byte) 0xF1, (byte) 0x40, (byte) 0x80, (byte) 0x2F, (byte) 0x5F, (byte) 0xFC};

    // https://wiki.multimedia.cx/index.php/ADTS
    //FF F1 5C 80 00 0F FC 21
//    FF F1 4C 80 00 1F FC
    public ADTS(byte[] payload, int offset, int size, ElementSpecific specific) {
        byte[] adtsHeader = getAdtsHeader(size, specific);
        raw = new byte[size + adtsHeader.length];
        System.arraycopy(adtsHeader, 0, raw, 0, adtsHeader.length);
        System.arraycopy(payload, offset, raw, adtsHeader.length, size);
    }

    private static byte[] getAdtsHeader(int size, ElementSpecific specific) {
        byte[] adtsHeader = new byte[]{(byte) 0xFF, (byte) 0xF1, (byte) 0x40, (byte) 0x80, (byte) 0x2F, (byte) 0x5F, (byte) 0xFC};
        size += adtsHeader.length;
        adtsHeader[2] |= (byte) ((getByFrequency(specific.clockRate()).getFrequency()) << 2);
        adtsHeader[3] |= (byte) ((size & 0x1800) >> 11);
        adtsHeader[4] = (byte) ((size & 0x1FF8) >> 3);
        adtsHeader[5] = (byte) ((size & 0x7) << 5);
        return adtsHeader;
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
        return adtsHeader.length;
    }
}
