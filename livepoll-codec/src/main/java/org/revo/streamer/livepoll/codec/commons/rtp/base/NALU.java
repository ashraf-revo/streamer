package org.revo.streamer.livepoll.codec.commons.rtp.base;


import lombok.Getter;

@Getter
public class NALU extends Packet {
    public final static byte[] NALUPrefix = {0x00, 0x00, 0x00, 0x01};
    private byte[] payload;
    private final NaluHeader NALUHeader;

    public NALU(byte[] payload, int offset, int length) {
        this.payload = new byte[length - offset];
        System.arraycopy(payload, offset, this.payload, 0, this.payload.length);
        this.NALUHeader = NaluHeader.read(payload[offset]);
    }


    public NALU(int F, int NRI, int TYPE) {
        this.payload = new byte[1];
        this.NALUHeader = NaluHeader.from(F, NRI, TYPE);
        this.payload[0] = this.NALUHeader.getRaw();
    }

    private static byte[] copyOfAndAppend(byte[] data1, byte[] data2) {
        byte[] result = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, result, 0, data1.length);
        System.arraycopy(data2, 0, result, data1.length, data2.length);
        return result;
    }

    public void appendPayload(byte[] data, int offset) {
        byte[] ndata = new byte[data.length - offset];
        System.arraycopy(data, offset, ndata, 0, ndata.length);
        this.payload = copyOfAndAppend(this.payload, ndata);
    }

    @Override
    public byte[] getRaw() {
        byte[] bytes = new byte[NALUPrefix.length + payload.length];
        System.arraycopy(NALUPrefix, 0, bytes, 0, NALUPrefix.length);
        System.arraycopy(payload, 0, bytes, NALUPrefix.length, payload.length);
        return bytes;
    }


    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public int headerSize() {
        return NALUPrefix.length;
    }

    @Getter
    public static class NaluHeader {
        public static final int HIGH = 3;
        public static final int MIDUM = 2;
        public static final int LOW = 1;
        public static final int IGNORE = 0;
        private int F;
        private int NRI;
        private int TYPE;

        private NaluHeader() {

        }

        NaluHeader(int F, int NRI, int TYPE) {
            this.F = F;
            this.NRI = NRI;
            this.TYPE = TYPE;
        }

        public static NaluHeader read(byte b) {
            NaluHeader naluHeader = new NaluHeader();
            naluHeader.F = (b & 0x80) >> 7;
            naluHeader.NRI = (b & 0x60) >> 5;
            naluHeader.TYPE = b & 0x1F;
            return naluHeader;
        }

        public static boolean isH264IdrKeyFrame(byte b0, byte b1) {
            NaluHeader read_0 = NaluHeader.read(b0);
            NaluHeader read_1 = NaluHeader.read(b1);
            return (read_0.getNRI() == 3 && read_0.getTYPE() == 28 && read_1.getTYPE() == 5 && read_1.getF() == 1);
        }

        public static boolean isH264IdrFrame(byte b0, byte b1) {
            NaluHeader read_0 = NaluHeader.read(b0);
            NaluHeader read_1 = NaluHeader.read(b1);
            return (read_0.getNRI() == 3 && read_0.getTYPE() == 28 && read_1.getTYPE() == 5);
        }

        static NaluHeader from(int F, int NRI, int TYPE) {
            return new NaluHeader(F, NRI, TYPE);
        }

        @Override
        public String toString() {
            return "NaluHeader{" +
                    "F=" + F +
                    ", NRI=" + NRI +
                    ", TYPE=" + TYPE +
                    '}';
        }


        byte getRaw() {
            int i = ((this.F << 7) | (this.NRI << 5) | (this.TYPE & 0x1F)) & 0xFF;
            return ((byte) i);
        }
    }
}
