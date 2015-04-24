package name.chabs.proxyapp.services;

import name.chabs.proxyapp.beans.ConfigBean;
import name.chabs.proxyapp.exceptions.ConfigException;

import java.io.File;

/**
 * Interface which provides method to handle configuration loading from
 * different sources
 */
public interface ConfigService {

    int DEFAULT_LISTEN_PORT = 8888;

    String CONFIG_FILE_NAME = "proxy-app";
    String CONFIG_FILE_SUFFIX = "properties";
    String CONFIG_FILE = CONFIG_FILE_NAME + CONFIG_FILE_SUFFIX;
    String USER_HOME = System.getProperty("user.home");

    String OPTION_URL = "proxy-url";
    String OPTION_URL_HELP = "Proxy url (credentials could be passe using http://user:password@host syntax)";
    String OPTION_PORT = "port";
    String OPTION_PORT_HELP = "Listening port for proxy. Default to " + DEFAULT_LISTEN_PORT;

    // Configuration properties
    String CONF_PROXY_APP_URL = "proxy.app.url";
    String CONF_PROXY_APP_PORT = "proxy.app.port";

    String APP_NAME = "ProxyApp";

    /**
     * Load proxy configuration from command line arguments.
     *
     * @param options Command line arguments
     * @return {@link ConfigBean} if command line arguments could be parsed, null otherwise.
     * @throws ConfigException if there is a problem parsing options, or if an option is missing.
     */
    ConfigBean loadConfiguration(String[] options) throws ConfigException;

    /**
     * Load proxy configuration from properties file specified.
     *
     * @param {@link File} file which contains configuration property to load
     * @return ConfigBean if it could be loaded, null otherwise.
     */
    ConfigBean loadConfiguration(File file) throws ConfigException;
}
