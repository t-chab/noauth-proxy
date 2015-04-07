package name.chabs;

import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Queue;

/**
 * Simple HTTP/HTTPS forward proxy.
 * Authentication for remote proxy is added on the fly for each queries.
 */
class ProxyApp {

    private final static String APP_NAME = ProxyApp.class.getName();
    private final static int DEFAULT_LISTEN_PORT = 8888;

    private final static String CONFIG_FILE_NAME = "proxy-app.properties";
    private final static String USER_HOME = System.getProperty("user.home");

    private final static String OPTION_URL = "url";
    private static final String OPTION_URL_HELP = "Proxy url (credentials could be passe using http://user:password@host syntax)";
    private static final String OPTION_PORT = "port";
    private static final String OPTION_PORT_HELP = "Listening port for proxy. Default to " + DEFAULT_LISTEN_PORT;

    private final static Logger logger = LoggerFactory.getLogger(ProxyApp.class);

    // Configuration properties
    private static final String CONF_PROXY_APP_URL = "proxy.app.url";
    private static final String CONF_PROXY_APP_PORT = "proxy.app.port";

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

        // Get proxy configuration to use
        ConfigBean lConf = loadConfiguration(args);

        if (lConf == null) {
            System.exit(1);
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
     * Load configuration from command line if specified, otherwise from file.
     *
     * @return {@link ConfigBean}, null if configuration can't be loaded for some reason.
     */
    private static ConfigBean loadConfiguration(String[] args) {
        ConfigBean lConf;

        // Adding options
        Options options = new Options();
        options.addOption(OPTION_URL, true, OPTION_URL_HELP);
        options.addOption(OPTION_PORT, true, OPTION_PORT_HELP);

        // Default configuration file
        final String lConfigFilePath = USER_HOME + File.pathSeparator + CONFIG_FILE_NAME;

        lConf = loadConfigFromCommandLine(options, args);
        if (lConf == null) {
            lConf = loadConfigFromFile(lConfigFilePath);
            if (lConf == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(APP_NAME, options);
            } else {
                logger.info("Configuration loaded from file {}", lConfigFilePath);
            }
        } else {
            logger.info("Configuration loaded from command line.");
        }

        if (lConf != null) {
            logger.info("Using forward proxy url {} - listening to port {}", lConf.getProxyUrl(), lConf.getProxyPort());
        }

        return lConf;
    }

    /**
     * Load proxy configuration from file path specified.
     *
     * @param filePath Path to config file
     * @return {@link ConfigBean} if it could be loaded, null otherwise.
     */
    private static ConfigBean loadConfigFromFile(String filePath) {
        ConfigBean lConf = null;

        File lConfig = new File(filePath);
        Properties lProps = new Properties();
        try {
            lProps.load(new FileReader(lConfig));

            URL lProxyUrl = null;
            final String lPropProxyUrl = lProps.getProperty(CONF_PROXY_APP_URL);
            if (StringUtils.isNotBlank(lPropProxyUrl)) {
                try {
                    lProxyUrl = new URL(lPropProxyUrl);
                } catch (MalformedURLException ex) {
                    logger.error("Url {} is invalid : {}", lPropProxyUrl, ex.getMessage());
                }
            }

            int lPort = DEFAULT_LISTEN_PORT;
            final String lPropPort = lProps.getProperty(CONF_PROXY_APP_PORT);
            if (StringUtils.isNumeric(lPropPort)) {
                try {
                    lPort = Integer.parseInt(lPropPort);
                } catch (NumberFormatException e) {
                    logger.error("Specified port number {} is invalid : {}", lPropPort, e.getMessage());
                }
            }

            if (lProxyUrl != null) {
                lConf = new ConfigBean(lProxyUrl, lPort);
            }
        } catch (FileNotFoundException ex) {
            logger.info("No configuration file found for path {}", filePath);
        } catch (IOException e) {
            logger.warn("Error {} reading config file {}", e.getMessage(), filePath);
        }

        return lConf;
    }

    /**
     * Load proxy configuration from command line arguments.
     *
     * @param options Options to parse
     * @param args    Command line arguments
     * @return {@link ConfigBean} if command line arguments could be parsed, null otherwise.
     */
    private static ConfigBean loadConfigFromCommandLine(Options options, String[] args) {
        ConfigBean lConf = null;
        CommandLineParser parser = new GnuParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            logger.error("Parsing failed.  Reason: " + exp.getMessage());
        }

        if (cmd != null) {
            URL lProxyUrl = null;
            if (cmd.hasOption(OPTION_URL)) {
                final String lUrlOptionValue = cmd.getOptionValue(OPTION_URL);
                try {
                    lProxyUrl = new URL(lUrlOptionValue);
                } catch (MalformedURLException ex) {
                    logger.error("Url {} is invalid : {}", lUrlOptionValue, ex.getMessage());
                }
            }

            int lPort = DEFAULT_LISTEN_PORT;
            if (cmd.hasOption(OPTION_PORT)) {
                final String lOptionPort = cmd.getOptionValue(OPTION_PORT);
                try {
                    lPort = Integer.parseInt(lOptionPort);
                } catch (NumberFormatException e) {
                    logger.error("Specified port number {} is invalid : {}", lOptionPort, e.getMessage());
                }
            }

            if (lProxyUrl != null) {
                lConf = new ConfigBean(lProxyUrl, lPort);
            }
        }

        return lConf;
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
