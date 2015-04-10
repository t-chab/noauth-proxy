package name.chabs.proxyapp;

import io.netty.handler.codec.http.HttpRequest;
import name.chabs.proxyapp.beans.ConfigBean;
import name.chabs.proxyapp.exceptions.ConfigException;
import name.chabs.proxyapp.services.ConfigService;
import name.chabs.proxyapp.services.ConfigServiceImpl;
import org.apache.commons.cli.ParseException;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;

/**
 * Simple HTTP/HTTPS forward proxy.
 * Authentication for remote proxy is added on the fly for each queries.
 */
class ProxyApp {

    private final static Logger logger = LoggerFactory.getLogger(ProxyApp.class);

    /**
     * Parse arguments and create a new proxy server which forwards
     * all queries to <em>proxyUrl</em>.
     * <p/>
     * Allowed arguments (in this order) are :
     * <ul>
     * <li><em>proxyUrl</em> : URL of proxy which handle forwarded queries.
     * Username and password should be passed in URL, after protocol.</li>
     * <li><em>port</em> : (optional) port number to listen. 8888 is used by default.</li>
     * </ul>
     */
    public static void main(String[] args) throws MalformedURLException, ParseException {

        // Get instance of config service
        ConfigService lConfSrv = new ConfigServiceImpl();

        // Get proxy configuration to use

        ConfigBean lConf = null;
        try {
            lConf = lConfSrv.loadConfiguration(args);
        } catch (ConfigException e) {
            logger.warn("Unable to load configuration from command line arguments : {}", e.getMessage());
        }

        if (lConf == null) {
            final String fileName = ConfigService.CONFIG_FILE_NAME;
            logger.info("Trying to load configuration from {} file.", fileName);
            try {
                lConf = lConfSrv.loadConfiguration(new File(fileName));
            } catch (ConfigException e) {
                logger.error("Unable to load configuration from file {} : {}", fileName, e.getMessage());
                System.exit(-1);
            }
        }

        final URL lProxyUrl = lConf.getProxyUrl();
        logger.info("Using url {}", lProxyUrl);
        int listenPort = lConf.getProxyPort();
        DefaultHttpProxyServer.bootstrap()
                .withPort(listenPort)
                .withChainProxyManager(chainedProxyManager(lProxyUrl))
                .start();
        logger.info("Started and listening to {}", listenPort);
    }

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
}
