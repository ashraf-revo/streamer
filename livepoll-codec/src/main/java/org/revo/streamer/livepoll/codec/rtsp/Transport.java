package org.revo.streamer.livepoll.codec.rtsp;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * rtsp 协议的头信息中， Transoprt 属性的实例化对象
 *
 * @author 陈修恒
 * @date 2016年4月15日
 */
public class Transport {
    static final String RTP_AVP_TCP = "RTP/AVP/TCP";
    static final String UNICAST = "unicast";
    private static final String INTERLEAVED = "interleaved";

    private String tranport = RTP_AVP_TCP;
    private String unicast = UNICAST;

    private Map<String, String> parameters = new HashMap<>();

    public static Transport rtpOnTcp(int rtpChannel, int rtcpChannel) {
        Transport t = new Transport();
        t.parameters.put(INTERLEAVED, rtpChannel + "-" + rtcpChannel);

        return t;
    }

    public static Transport parse(String transport) {
        if (null == transport) {
            throw new IllegalArgumentException("Transport parse NULL");
        }

        Transport t = new Transport();
        String[] splits = StringUtils.split(transport, ';');
        t.tranport = splits[0];
        t.unicast = splits[1];
        for (String split : splits) {
            if (split.contains("=")) {
                String[] keyValue = StringUtils.split(split, '=');
                t.parameters.put(keyValue[0], keyValue[1]);
            }
        }

        return t;
    }

    public String getTranport() {
        return tranport;
    }

    public void setTranport(String protocol) {
        this.tranport = protocol;
    }

    public String getUnicast() {
        return unicast;
    }

    public void setUnicast(String castMode) {
        this.unicast = castMode;
    }

    /**
     * @return null if Parameter Not Found
     */
    public int[] getInterleaved() {
        String v = this.parameters.get(INTERLEAVED);
        if (null == v) {
            return null;
        }

        String[] nums = StringUtils.split(v, '-');
        int[] values = new int[nums.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Integer.parseInt(nums[i]);
        }

        return values;
    }

    public void setSsrc(long ssrc) {
        this.parameters.put("ssrc", Long.toHexString(ssrc));
    }

    public void setParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(tranport);
        buf.append(";").append(unicast);

        String key;

        key = "interleaved";
        if (parameters.containsKey(key)) {
            buf.append(";").append(key).append("=").append(parameters.get(key));
        }

        key = "mode";
        if (parameters.containsKey(key)) {
            buf.append(";").append(key).append("=").append(parameters.get(key));
        }


        key = "ssrc";
        if (parameters.containsKey(key)) {
            buf.append(";").append(key).append("=").append(parameters.get(key));
        }

        return buf.toString();
    }
}
