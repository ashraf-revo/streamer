package org.revo.streamer.livepoll.codec.commons.utils;

import lombok.Getter;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL 对象
 *
 * <p>
 * 把普通的 url 地址， 如 'rtsp://user:pass@host:port/uri' ，解析成方便使用的对象
 *
 * @author 陈修恒
 * @date 2016年4月15日
 */
@Getter
public class URLObject {
    private final String scheme;
    private final String user;
    private final String password;
    private final String host;
    private final int port;
    private final String uri;


    public URLObject(String url) throws MalformedURLException {
        Matcher m = match(url);
        if (!m.find()) {
            throw new MalformedURLException("非法的 RTSP 地址[" + url + "]");
        }

        scheme = m.group(1);
        user = m.group(3);
        password = m.group(4);
        host = m.group(5);

        int defaultPort = 80;
        switch (scheme) {
            case "rtsp":
                defaultPort = 554;
                break;
            case "rtmp":
                defaultPort = 1935;
                break;
            case "https":
                defaultPort = 443;
                break;
        }

        String portString = m.group(7);
        try {
            if (null != portString) {
                defaultPort = Integer.parseInt(portString);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("'" + portString + "' is NOT a port value");
        }

        port = defaultPort;
        uri = m.group(8);

    }

    private static Matcher match(String url) {
        Pattern pattern = Pattern.compile("^([^:]+)://(([^:]+):([^@]*)@)?([^:/]+)(:([0-9]+))?([^\\?]*)");
        return pattern.matcher(url);
    }

    public static String getUri(String url) {
        if (null == url) {
            return null;
        }

        Matcher matcher = match(url);
        if (matcher.find()) {
            return matcher.group(8);
        } else {
            return url;
        }
    }

    public static String getId(String url) {
        return Paths.get(getUri(url)).getFileName().toString();
    }

    public String getUrl() {
        return scheme + "://" + host + ":" + port + uri;
    }

    @Override
    public String toString() {
        return getUrl();
    }

}
