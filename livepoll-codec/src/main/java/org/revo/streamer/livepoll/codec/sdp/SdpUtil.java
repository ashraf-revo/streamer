package org.revo.streamer.livepoll.codec.sdp;

import gov.nist.core.StringTokenizer;
import gov.nist.javax.sdp.MediaDescriptionImpl;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.fields.SDPField;
import gov.nist.javax.sdp.parser.ParserFactory;
import gov.nist.javax.sdp.parser.SDPParser;
import org.apache.commons.lang.StringUtils;

import javax.sdp.SdpException;
import javax.sdp.SessionDescription;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class SdpUtil {

    public static Map<String, List<AttributeField>> getAttributeFields(SessionDescription sessionDescription, String filter) {
        Map<String, List<AttributeField>> result = new HashMap<>();
        try {
            List<MediaDescriptionImpl> list = ((ArrayList<MediaDescriptionImpl>) Collections.list(sessionDescription
                    .getMediaDescriptions(true).elements()));
            for (MediaDescriptionImpl mediaDescription : list) {
                List<AttributeField> et = new ArrayList<>();

                for (AttributeField field : (ArrayList<AttributeField>) Collections.
                        list(mediaDescription.getAttributes(true).elements())) {
                    if (field.getName().equals(filter)) {
                        et.add(field);
                    }
                }
                result.put(mediaDescription.getMedia().getMediaType(), et);

            }
        } catch (SdpException ignored) {
        }
        return result;
    }

    public static List<String> getSpropParameter(SessionDescription sessionDescription) {
        return getAttributeFields(sessionDescription, "fmtp").get("video").stream()
                .map(it -> it.getAttribute().getValue().split("; ")[1]
                        .replace("sprop-parameter-sets=", "")).collect(Collectors.toList());
    }

    static public SessionDescriptionImpl withSdp(String sdp) {
        SessionDescriptionImpl sd = new SessionDescriptionImpl();

        if (!isEmpty(sdp)) {
            gov.nist.core.StringTokenizer tokenizer = new StringTokenizer(sdp);
            while (tokenizer.hasMoreChars()) {
                String line = tokenizer.nextToken();

                try {
                    SDPParser paser = ParserFactory.createParser(line);
                    if (null != paser) {
                        SDPField obj = paser.parse();
                        sd.addField(obj);
                    }
                } catch (ParseException e) {
//                    logger.warn("fail parse [{}]", line, e);
                }
            }

        }
        return sd;
    }
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

}
