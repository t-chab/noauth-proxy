package name.chabs;

import java.net.URL;

/**
 * Config
 */
class ConfigBean {
    private final URL proxyUrl;
    private final int proxyPort;

    ConfigBean(URL url, int port) {
        proxyUrl = url;
        proxyPort = port;
    }

    URL getProxyUrl() {
        return proxyUrl;
    }

    int getProxyPort() {
        return proxyPort;
    }
}
