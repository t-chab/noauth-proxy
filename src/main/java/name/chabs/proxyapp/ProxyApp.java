package name.chabs.proxyapp;

import name.chabs.proxyapp.beans.ConfigBean;
import name.chabs.proxyapp.exceptions.ConfigException;
import name.chabs.proxyapp.services.ConfigService;
import name.chabs.proxyapp.services.ConfigServiceImpl;
import name.chabs.proxyapp.services.ProxyService;
import name.chabs.proxyapp.services.ProxyServiceImpl;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simple HTTP/HTTPS forward proxy.
 * Authentication for remote proxy is added on the fly for each queries.
 */
class ProxyApp {

    private final static Logger logger = LoggerFactory.getLogger(ProxyApp.class);

    private final static String HOME_DIRECTORY = System.getProperty("user.home");

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

        // Trying to load configuration from command line
        ConfigBean lConf = null;
        try {
            lConf = lConfSrv.loadConfiguration(args);
        } catch (ConfigException e) {
            logger.info("Unable to load configuration from command line arguments : {}", e.getMessage());
        }

        // If not command line arguments specified, try to load conf from file
        if (lConf == null) {
            final String fileName = HOME_DIRECTORY + File.pathSeparator + ConfigService.CONFIG_FILE;
            logger.info("Trying to load configuration from {} file.", fileName);
            try {
                lConf = lConfSrv.loadConfiguration(new File(fileName));
            } catch (ConfigException e) {
                logger.error("Unable to load configuration from file {} : {}", fileName, e.getMessage());
                System.exit(-1);
            }
        }

        // Start forward proxy
        final URL lProxyUrl = lConf.getProxyUrl();
        logger.info("Using url {}", lProxyUrl);
        ProxyService lProxy = new ProxyServiceImpl();
        lProxy.launchForwardProxy(lConf.getProxyPort(), lConf.getProxyUrl());
        logger.info("Started and listening to {}", lProxyUrl.getPort());
    }
}
