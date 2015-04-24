package name.chabs.proxyapp.services;

import java.net.URL;

/**
 * This interface defines proxy management method
 */
public interface ProxyService {
    /**
     * Start a new forwarding proxy server,
     * listening to specified port, and forwarding requests to specified proxy url
     */
    void launchForwardProxy(int port, URL targetProxy);
}
