package org.revo.streamer.livepoll.mpeg2ts.commons.container.m3u8.video.mpeg2ts;


/**
 * --------------------------------- PES/TS 概述 -----------------------------------------
 * <p>
 * PES  (  一个 PES 被切成N段，每段组成一个TS包的负载 )
 * TS 1
 * TS 2
 * TS ...
 * TS N
 * 第一个TS包 = TS头 + TS自适应字段（8bit） + PES头 + PES可选头+ NALU AUD + SEI + SPS + PPS + TS负载(IDR Frame)
 * 第二个TS包到本帧倒数第二个TS包:
 * 固定的格式:  TS = TS_header(4bytes) + TS_payload (184bytes)
 * 唯一变化的就是Ts_header中的字段 ContinuityCounter(包递增计数器), 从0-15循环变化
 * 最后一个TS包 = TS头 + TS自适应字段 + 填充字段 + TS Payload
 *
 * @author zhuam
 * @see <a href="https://en.m.wikipedia.org/wiki/MPEG_transport_stream">see</a>
 * @see <a href="https://en.m.wikipedia.org/wiki/MPEG_program_stream">see</a>
 * @see <a href="https://en.m.wikipedia.org/wiki/Program-specific_information">see</a>
 */
public class TsWriter {

    // Transport Stream packets are 188 bytes in length
    public static final int TS_PACKET_SIZE = 188;
    public static final int TS_HEADER_SIZE = 4;
    public static final int TS_PAYLOAD_SIZE = TS_PACKET_SIZE - TS_HEADER_SIZE;

    // Table 2-29 – Stream type assignments. page 66
    public static final byte STREAM_TYPE_AUDIO_AAC = 0x0f;
    public static final byte STREAM_TYPE_AUDIO_MP3 = 0x03;
    public static final byte STREAM_TYPE_VIDEO_H264 = 0x1b;


    public static final int TS_PAT_PID = 0x0000;    // 0
    public static final int TS_PMT_PID = 0x1000;    // 4096
    public static final int TS_AUDIO_PID = 0x101;    // 257
    public static final int TS_VIDEO_PID = 0x100;    // 256

    // Transport Stream Description Table
    public static final int TS_PAT_TABLE_ID = 0x00;
    public static final int TS_PMT_TABLE_ID = 0x02;

    public static byte[] H264_NAL = {0x00, 0x00, 0x00, 0x01, 0x09, (byte) 0xf0};

    private byte mAudioContinuityCounter = 0;
    private byte mVideoContinuityCounter = 0;
    private int mPatContinuityCounter = 0;
    private int mPmtContinuityCounter = 0;

    private int write_ts_header(byte[] buf, int offset, int pid, int continuity_counter) {

        byte sync_byte = 0x47;
        int transport_error_indicator = 0;
        int payload_unit_start_indicator = 1;
        int transport_priority = 0;
        int transport_scrambling_control = 0;
        int adaptation_field_control = 1;

        buf[offset++] = sync_byte;
        buf[offset++] = (byte) ((transport_error_indicator << 7) | (payload_unit_start_indicator << 6) | (transport_priority << 5) | ((pid >> 8) & 0x1F));
        buf[offset++] = (byte) (pid & 0xff);
        buf[offset++] = (byte) ((transport_scrambling_control << 6) | (adaptation_field_control << 4) | (continuity_counter & 0x0F));
        buf[offset++] = 0x00;    //起始指示符

        return offset;
    }

    private byte[] write_pat() {

        byte[] tsBuf = new byte[TS_PACKET_SIZE];

        // fill 0xFF
        for (int i = 0; i < tsBuf.length; i++) {
            tsBuf[i] = (byte) 0xFF;
        }

        int offset = 0;

        // header
        offset = write_ts_header(tsBuf, offset, TS_PAT_PID, mPatContinuityCounter);
        mPatContinuityCounter = (mPatContinuityCounter + 1) & 0x0F; //包递增计数器(0-15)

        // PAT body
        int section_syntax_indicator = 1;
        int zero = 0;
        int reserved_1 = 3;
        int section_length = 13;
        int transport_stream_id = 1;
        int reserved_2 = 3;
        int version_number = 0;
        int current_next_indicator = 1;
        int section_number = 0;
        int last_section_number = 0;
        int program_number = 1;
        int reserved_3 = 7;
        int program_id = TS_PMT_PID;

        tsBuf[offset++] = TS_PAT_TABLE_ID & 0XFF;
        tsBuf[offset++] = (byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F));
        tsBuf[offset++] = (byte) (section_length & 0xFF);
        tsBuf[offset++] = (byte) ((transport_stream_id >> 8) & 0xFF);
        tsBuf[offset++] = (byte) (transport_stream_id & 0xFF);
        tsBuf[offset++] = (byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01));
        tsBuf[offset++] = (byte) (section_number & 0xFF);
        tsBuf[offset++] = (byte) (last_section_number & 0xFF);
        tsBuf[offset++] = (byte) ((program_number >> 8) & 0xFF);

        tsBuf[offset++] = (byte) (program_number & 0xFF);
        tsBuf[offset++] = (byte) ((reserved_3 << 5) | ((program_id >> 8) & 0x1F));
        tsBuf[offset++] = (byte) (program_id & 0xFF);

        // set crc32
        long crc = TsUtil.mpegts_crc32(tsBuf, 5, 12);
        tsBuf[offset++] = (byte) ((crc >> 24) & 0xFF);
        tsBuf[offset++] = (byte) ((crc >> 16) & 0xFF);
        tsBuf[offset++] = (byte) ((crc >> 8) & 0xFF);
        tsBuf[offset++] = (byte) ((crc) & 0xFF);

        return tsBuf;
    }

    /*
      only audio , section_length = 18
      audio & video mix, section_length = 23
     */
    private byte[] write_pmt(FrameDataType fType) {

        byte[] tsBuf = new byte[TS_PACKET_SIZE];

        // fill 0xFF
        for (int i = 0; i < tsBuf.length; i++) {
            tsBuf[i] = (byte) 0xFF;
        }

        //
        int offset = 0;

        // header
        offset = write_ts_header(tsBuf, offset, TS_PMT_PID, mPmtContinuityCounter);
        mPmtContinuityCounter = (mPmtContinuityCounter + 1) & 0x0F; //包递增计数器(0-15)

        // PMT body
        int table_id = TS_PMT_TABLE_ID;
        int section_syntax_indicator = 1;
        int zero = 0;
        int reserved_1 = 3;
        int section_length = (fType == FrameDataType.MIXED) ? 23 : 18;
        int program_number = 1;
        int reserved_2 = 3;
        int version_number = 0;
        int current_next_indicator = 1;
        int section_number = 0;
        int last_section_number = 0;
        int reserved_3 = 7;
        int pcr_pid = (fType == FrameDataType.AUDIO) ? TS_AUDIO_PID : TS_VIDEO_PID;
        int reserved_4 = 15;
        int program_info_length = 0;

        tsBuf[offset++] = (byte) table_id;
        tsBuf[offset++] = (byte) ((section_syntax_indicator << 7) | (zero << 6) | (reserved_1 << 4) | ((section_length >> 8) & 0x0F));
        tsBuf[offset++] = (byte) (section_length & 0xFF);
        tsBuf[offset++] = (byte) ((program_number >> 8) & 0xFF);
        tsBuf[offset++] = (byte) (program_number & 0xFF);
        tsBuf[offset++] = (byte) ((reserved_2 << 6) | (version_number << 1) | (current_next_indicator & 0x01));
        tsBuf[offset++] = (byte) section_number;
        tsBuf[offset++] = (byte) last_section_number;
        tsBuf[offset++] = (byte) ((reserved_3 << 5) | ((pcr_pid >> 8) & 0xFF));
        tsBuf[offset++] = (byte) (pcr_pid & 0xFF);
        tsBuf[offset++] = (byte) ((reserved_4 << 4) | ((program_info_length >> 8) & 0xFF));
        tsBuf[offset++] = (byte) (program_info_length & 0xFF);


        // set video stream info
        if (fType == FrameDataType.VIDEO || fType == FrameDataType.MIXED) {
            int stream_type = 0x1b;
            int reserved_5 = 7;
            int elementary_pid = TS_VIDEO_PID;
            int reserved_6 = 15;
            int ES_info_length = 0;

            tsBuf[offset++] = (byte) stream_type;
            tsBuf[offset++] = (byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F));
            tsBuf[offset++] = (byte) (elementary_pid & 0xFF);
            tsBuf[offset++] = (byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F));
            tsBuf[offset++] = (byte) (ES_info_length & 0xFF);
        }


        // set audio stream info
        if (fType == FrameDataType.AUDIO || fType == FrameDataType.MIXED) {

            int stream_type = 0x0f;
            int reserved_5 = 7;
            int elementary_pid = TS_AUDIO_PID;
            int reserved_6 = 15;
            int ES_info_length = 0;

            tsBuf[offset++] = (byte) stream_type;
            tsBuf[offset++] = (byte) ((reserved_5 << 5) | ((elementary_pid >> 8) & 0x1F));
            tsBuf[offset++] = (byte) (elementary_pid & 0xFF);
            tsBuf[offset++] = (byte) ((reserved_6 << 4) | ((ES_info_length >> 4) & 0x0F));
            tsBuf[offset++] = (byte) (ES_info_length & 0xFF);
        }


        // set crc32
        long crc = TsUtil.mpegts_crc32(tsBuf, 5, (fType == FrameDataType.MIXED) ? 22 : 17);
        tsBuf[offset++] = (byte) ((crc >> 24) & 0xFF);
        tsBuf[offset++] = (byte) ((crc >> 16) & 0xFF);
        tsBuf[offset++] = (byte) ((crc >> 8) & 0xFF);
        tsBuf[offset++] = (byte) ((crc) & 0xFF);

        return tsBuf;
    }

    /**
     * write a PTS or DTS
     *
     * @see <a href="http://github.com/kynesim/tstools/blob/master/ts.c">see</a>
     * @see <a href="http://www.ffmpeg.org/doxygen/0.6/mpegtsenc_8c-source.html">see</a>
     */
    private int write_pts_dts(byte[] buf, int offset, int guard_bits, long value) {


        //		tsBuf[offset++] = (byte) (((pts >> 29) & 0xFE) | 0x31);
        //		tsBuf[offset++] = (byte) ((pts >> 22) & 0xff);
        //		tsBuf[offset++] = (byte) (((pts >> 14) & 0xFE) | 0x01);
        //		tsBuf[offset++] = (byte) ((pts >> 7) & 0xff);
        //		tsBuf[offset++] = (byte) ((pts << 1) & 0xFE | 0x01);

        //
        int pts1 = (int) ((value >> 30) & 0x07);
        int pts2 = (int) ((value >> 15) & 0x7FFF);
        int pts3 = (int) (value & 0x7FFF);

        buf[offset++] = (byte) ((guard_bits << 4) | (pts1 << 1) | 0x01);
        buf[offset++] = (byte) ((pts2 & 0x7F80) >> 7);
        buf[offset++] = (byte) (((pts2 & 0x007F) << 1) | 0x01);
        buf[offset++] = (byte) ((pts3 & 0x7F80) >> 7);
        buf[offset++] = (byte) (((pts3 & 0x007F) << 1) | 0x01);

        return offset;
    }

    public byte[] writeH264(boolean isFirstPes, byte[] buf, long pts, long dts) {
        FrameData frameData = new FrameData();
        frameData.buf = buf;
        frameData.pts = pts;
        frameData.dts = dts;
        frameData.isAudio = false;

        return write(isFirstPes, FrameDataType.VIDEO, frameData);
    }

    public byte[] write(boolean isFirstPes, FrameDataType frameDataType, FrameData... frames) {

        byte[] tsFileBuffer = null;

        // write pat table
        byte[] patTsBuf = write_pat();
        tsFileBuffer = TsUtil.margeByteArray(tsFileBuffer, patTsBuf);

        // write pmt table
        byte[] pmtTsBuf = write_pmt(frameDataType);
        tsFileBuffer = TsUtil.margeByteArray(tsFileBuffer, pmtTsBuf);

        //boolean isFirstVideoTs = true;

        //
        for (FrameData frame : frames) {

            boolean isFristTs = true;

            boolean isAudio = frame.isAudio;

            long pts = frame.pts;
            long dts = frame.dts;
            byte[] frameBuf = frame.buf;
            int frameBufSize = frameBuf.length;
            int frameBufPtr = 0;

            int pid = isAudio ? TS_AUDIO_PID : TS_VIDEO_PID;

            //
            while (frameBufPtr < frameBufSize) {

                int frameBufRemaining = frameBufSize - frameBufPtr;

                boolean isAdaptationField = (isFristTs || (frameBufRemaining < TS_PAYLOAD_SIZE)) ? true : false;
                byte[] tsBuf = new byte[TS_PACKET_SIZE];

                // write ts header
                int offset = 0;
                tsBuf[offset++] = 0x47;
                tsBuf[offset++] = (byte) ((isFristTs ? 0x40 : 0x00) | ((pid >> 8) & 0x1f));
                tsBuf[offset++] = (byte) (pid & 0xff);
                tsBuf[offset++] = (byte) ((isAdaptationField ? 0x30 : 0x10) | ((isAudio ? mAudioContinuityCounter++ : mVideoContinuityCounter++) & 0xF));

                if (isFristTs) {

                    tsBuf[offset++] = 0x07;                                                                    // size
                    tsBuf[offset++] |= isFirstPes ? 0x50 : (isAudio && frameDataType == FrameDataType.MIXED ? 0x50 : 0x10);
                    // flag bits 0001 0000 , 0x10
                    // flag bits 0101 0000 , 0x50
                    /* write PCR */
                    long pcr = pts;
                    tsBuf[offset++] = (byte) ((pcr >> 25) & 0xFF);
                    tsBuf[offset++] = (byte) ((pcr >> 17) & 0xFF);
                    tsBuf[offset++] = (byte) ((pcr >> 9) & 0xFF);
                    tsBuf[offset++] = (byte) ((pcr >> 1) & 0xFF);
                    tsBuf[offset++] = 0x00; //(byte) (pcr << 7 | 0x7E); // (6bit) reserved， 0x00
                    tsBuf[offset++] = 0x00;


                    /* write PES HEADER */
                    tsBuf[offset++] = 0x00;
                    tsBuf[offset++] = 0x00;
                    tsBuf[offset++] = 0x01;
                    tsBuf[offset++] = isAudio ? (byte) 0xc0 : (byte) 0xe0;

                    int header_size = 5 + 5;

                    // PES 包长度
                    if (isAudio) {
                        int pes_size = frameBufSize + header_size + 3;
                        tsBuf[offset++] = (byte) ((pes_size >> 8) & 0xFF);
                        tsBuf[offset++] = (byte) (pes_size & 0xFF);
                    } else {
                        tsBuf[offset++] = 0x00; // 为0表示不受限制
                        tsBuf[offset++] = 0x00; // 16:
                    }


                    // PES 包头识别标志
                    byte PTS_DTS_flags = (byte) 0xc0;
                    tsBuf[offset++] = (byte) 0x80;            // 0x80 no flags set,  0x84 just data alignment indicator flag set
                    tsBuf[offset++] = PTS_DTS_flags;        // 0xC0 PTS & DTS,  0x80 PTS,  0x00 no PTS/DTS
                    tsBuf[offset++] = (byte) header_size;    // 0x0A PTS & DTS,  0x05 PTS,  0x00 no

                    // write pts & dts
                    if (PTS_DTS_flags == (byte) 0xc0) {

                        //PTS_DTS_flags >> 6
                        offset = write_pts_dts(tsBuf, offset, 3, pts);
                        offset = write_pts_dts(tsBuf, offset, 1, dts);

                    } else if (PTS_DTS_flags == (byte) 0x80) {

                        offset = write_pts_dts(tsBuf, offset, 2, pts);
                    }


                    // H264 NAL
                    if (!isAudio && Bytes.indexOf(frameBuf, H264_NAL) == -1) {
                        System.arraycopy(H264_NAL, 0, tsBuf, offset++, H264_NAL.length);
                        offset += H264_NAL.length;
                    }

                } else {

                    // has adaptation
                    if (isAdaptationField) {
                        tsBuf[offset++] = 1;
                        tsBuf[offset++] = (byte) 0x00;

                    } else {
                        // no adaptation
                        // ts_header + ts_payload
                    }

                }


                // fill data
                int tsBufRemaining = TS_PACKET_SIZE - offset;
                if (frameBufRemaining >= tsBufRemaining) {

                    System.arraycopy(frameBuf, frameBufPtr, tsBuf, offset, tsBufRemaining);
                    offset += tsBufRemaining;
                    frameBufPtr += tsBufRemaining;

                } else {

                    int paddingSize = tsBufRemaining - frameBufRemaining;

                    // 0x30  0011 0000
                    // 0x10  0001 0000
                    // has adaptation
                    if (isAdaptationField) {

                        int adaptationFieldLength = (tsBuf[4] & 0xFF);
                        int start = TS_HEADER_SIZE + adaptationFieldLength + 1;
                        int end = offset - 1;

                        // DEBUG
                        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        //System.out.println("start=" + start + ", end=" + end);

                        // move
                        for (int i = end; i >= start; i--) {
                            tsBuf[i + paddingSize] = tsBuf[i];
                        }

                        // fill data, 0xff
                        for (int i = 0; i < paddingSize; i++) {
                            tsBuf[start + i] = (byte) 0xff;
                        }

                        tsBuf[4] += paddingSize;

                        // no adaptation
                    } else {

                        // set adaptation
                        tsBuf[3] |= 0x20;
                        tsBuf[4] = (byte) paddingSize;
                        tsBuf[5] = 0;

                        for (int i = 0; i < paddingSize; i++) {
                            tsBuf[6 + i] = (byte) 0xFF;
                        }
                    }

                    System.arraycopy(frameBuf, frameBufPtr, tsBuf, (int) (offset + paddingSize), frameBufRemaining);
                    frameBufPtr += frameBufRemaining;

                }

                isFristTs = false;

                tsFileBuffer = TsUtil.margeByteArray(tsFileBuffer, tsBuf);
            }
        }

        return tsFileBuffer;
    }

    public void reset() {
        mPatContinuityCounter = 0;
        mPmtContinuityCounter = 0;
        mAudioContinuityCounter = 0;
        mVideoContinuityCounter = 0;
    }


}

