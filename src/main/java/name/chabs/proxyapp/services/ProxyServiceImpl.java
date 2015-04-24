package name.chabs.proxyapp.services;

import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.net.URL;
import java.util.Queue;

/**
 * @see name.chabs.proxyapp.services.ProxyService
 */
public class ProxyServiceImpl implements ProxyService {
    /**
     * Create a new Proxy instance, and chained all request to remote proxy
     * pointed by proxyUrl.
     *
     * @param proxyUrl : {@link URL} of the remote proxy to use.
     */
    private static ChainedProxyManager chainedProxyManager(final URL proxyUrl) {
        return new ChainedProxyManager() {
            public void lookupChainedProxies(HttpRequest httpRequest,
                                             Queue<ChainedProxy> chainedProxies) {
                chainedProxies.add(new UpstreamProxy(proxyUrl));
            }
        };
    }

    public void launchForwardProxy(int port, URL targetProxy) {
        if (targetProxy == null || (port < 1) || (port > 65535)) {
            throw new IllegalArgumentException("No URL or invalide port number");
        }

        int listenPort = port;
        DefaultHttpProxyServer.bootstrap()
                .withPort(listenPort)
                .withChainProxyManager(chainedProxyManager(targetProxy))
                .start();
    }
}
