package org.revo.streamer.livepoll.commons.rtp.base;

public class AdtsFrame extends Packet {
    private byte[] raw;

    //FF F1 5C 80 00 0F FC 21
//    FF F1 4C 80 00 1F FC
    public AdtsFrame(byte[] payload, int offset, int size) {
        byte[] adtsHeader = getAdtsHeader(size);
        raw = new byte[size + adtsHeader.length];
        System.arraycopy(adtsHeader, 0, raw, 0, adtsHeader.length);
        System.arraycopy(payload, offset, raw, adtsHeader.length, size);
    }

    private static byte[] getAdtsHeader(int size) {
        byte[] adtsHeader = new byte[]{(byte) 0xFF, (byte) 0xF1, (byte) 0x4C, (byte) 0x80, (byte) 0x2F, (byte) 0x5F, (byte) 0xFC};
        size += adtsHeader.length;
        adtsHeader[3] |= (byte) ((size & 0x1800) >> 11);
        adtsHeader[4] = (byte) ((size & 0x1FF8) >> 3);
        adtsHeader[5] = (byte) ((size & 0x7) << 5);
        return adtsHeader;
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }

    public static byte[] getRaw(byte[] payload) {
        byte[] adtsHeader = getAdtsHeader(payload.length);
        byte[] raw = new byte[payload.length + adtsHeader.length];
        System.arraycopy(adtsHeader, 0, raw, 0, adtsHeader.length);
        System.arraycopy(payload, 0, raw, adtsHeader.length, payload.length);
        return raw;
    }

    @Override
    public byte[] getPayload() {
        byte[] adtsHeader = new byte[]{(byte) 0xFF, (byte) 0xF1, (byte) 0x4C, (byte) 0x80, (byte) 0x2F, (byte) 0x5F, (byte) 0xFC};
        byte[] payload = new byte[raw.length - adtsHeader.length];
        System.arraycopy(raw, adtsHeader.length, payload, 0, payload.length);
        return payload;
    }
}
