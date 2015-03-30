package name.chabs;

import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;

/**
 * Simple HTTP/HTTPS forward proxy.
 * Authentication for remote proxy is added on the fly for each queries.
 */
class ProxyApp {

    private final static int DEFAULT_LISTEN_PORT = 5555;

    private final static Logger logger = LoggerFactory.getLogger(ProxyApp.class);

    /**
     * Parse arguments and create a new proxy server which forwards
     * all queries to <em>proxyUrl</em>.
     * <p/>
     * Allowed arguments (in this order) are :
     * <ul>
     * <li><em>proxyUrl</em> : URL of proxy which handle forwarded queries.
     * Username and password should be passed in URL, after protocol.</li>
     * <li><em>port</em> : (optional) port number to listen. 5555 is used by default.</li>
     * </ul>
     */
    public static void main(String[] args) throws MalformedURLException {

        if (args == null || args.length < 0 || args.length > 2) {
            System.out.println("Usage : ProxyApp <proxyUrl> [listeningPort]");
        }

        URL lProxyUrl = null;
        if(args != null && args.length > 0) {
            lProxyUrl = new URL(args[0]);
            logger.debug("Using url {}", lProxyUrl);
        }

        if(lProxyUrl == null) {
            System.out.println("A forward proxy url must be specified !");
        } else {
            int listenPort = DEFAULT_LISTEN_PORT;
            if (args.length > 1) {
                try {
                    listenPort = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    logger.warn("NumberFormatException trying to parse port number {}. Default port number {} will be used instead",
                            args[1], DEFAULT_LISTEN_PORT);
                }
            }

            DefaultHttpProxyServer.bootstrap()
                    .withPort(listenPort)
                    .withChainProxyManager(chainedProxyManager(lProxyUrl))
                    .start();
            logger.info("Started and listening to {}", listenPort);
        }
    }

    /**
     * Create a new Proxy instance, and chained all request to remote proxy
     * pointed by proxyUrl.
     *
     * @param proxyUrl : {@link URL} of the remote proxy to use.
     */
    private static ChainedProxyManager chainedProxyManager(final URL proxyUrl) {
        return new ChainedProxyManager() {
            @Override
            public void lookupChainedProxies(HttpRequest httpRequest,
                                             Queue<ChainedProxy> chainedProxies) {
                chainedProxies.add(new UpstreamProxy(proxyUrl));
            }
        };
    }
}
