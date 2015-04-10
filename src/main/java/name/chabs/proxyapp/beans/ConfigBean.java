package name.chabs.proxyapp.beans;

import java.net.URL;

/**
 * POJO which contains configuration properties
 */
public class ConfigBean {
    private final URL proxyUrl;
    private final int proxyPort;

    public ConfigBean(URL url, int port) {
        proxyUrl = url;
        proxyPort = port;
    }

    public URL getProxyUrl() {
        return proxyUrl;
    }

    public int getProxyPort() {
        return proxyPort;
    }
}
