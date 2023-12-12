package org.revo.streamer.livepoll.codec.rtsp;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Transport {
    static final String RTP_AVP_TCP = "RTP/AVP/TCP";
    static final String UNICAST = "unicast";
    private static final String INTERLEAVED = "interleaved";

    private String tranport = RTP_AVP_TCP;
    private String unicast = UNICAST;

    private final Map<String, String> parameters = new HashMap<>();


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
}
