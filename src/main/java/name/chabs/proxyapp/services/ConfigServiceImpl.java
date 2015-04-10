package name.chabs.proxyapp.services;

import name.chabs.proxyapp.beans.ConfigBean;
import name.chabs.proxyapp.exceptions.ConfigException;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Default implementation for configuration loading service
 */

public class ConfigServiceImpl implements ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

    private static final String CONFIGURATION_MISSING_OPTION = "Can't load configuration : option " + OPTION_URL + " missing";
    private static final String CONFIGURATION_UNABLE_TO_PARSE = "Can't parse command line options.";
    private static final String CONFIGURATION_WRONG_URL_VALUE = "Invalid URL value.";

    public ConfigBean loadConfiguration(File file) throws ConfigException {
        ConfigBean lConf = null;

        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File is null or does not exists.");
        }

        Properties lProps = new Properties();
        try {
            lProps.load(new FileReader(file));

            URL lProxyUrl = null;
            final String lPropProxyUrl = lProps.getProperty(CONF_PROXY_APP_URL);
            if (StringUtils.isNotBlank(lPropProxyUrl)) {
                try {
                    lProxyUrl = new URL(lPropProxyUrl);
                } catch (MalformedURLException ex) {
                    logger.error("Url {} is invalid : {}", lPropProxyUrl, ex.getMessage());
                    throw new ConfigException("Invalid Url", ex);
                }
            }

            int lPort = DEFAULT_LISTEN_PORT;
            final String lPropPort = lProps.getProperty(CONF_PROXY_APP_PORT);
            if (StringUtils.isNumeric(lPropPort)) {
                try {
                    lPort = Integer.parseInt(lPropPort);
                } catch (NumberFormatException e) {
                    logger.warn("Specified port number {} is invalid : {} - using default port.", lPropPort, e.getMessage(), DEFAULT_LISTEN_PORT);
                }
            }

            if (lProxyUrl != null) {
                lConf = new ConfigBean(lProxyUrl, lPort);
            }
        } catch (FileNotFoundException e) {
            logger.error("No configuration file found for path {}", file.getAbsolutePath());
            throw new ConfigException("No configuration file found for specified path.", e);
        } catch (IOException e) {
            logger.error("Error {} reading config file {}", e.getMessage(), file.getAbsolutePath());
            throw new ConfigException("Error reading specified file.", e);
        }

        return lConf;
    }

    public ConfigBean loadConfiguration(String[] options) throws ConfigException {
        ConfigBean lConf = null;

        CommandLineParser cmdLineParser = new PosixParser();

        Options cmdOptions = new Options();
        cmdOptions.addOption(OptionBuilder.withLongOpt("proxy-url")
                .withDescription(OPTION_URL_HELP)
                .withType(URL.class)
                .hasArg()
                .withArgName("proxy-url")
                .create());
        cmdOptions.addOption(OptionBuilder.withLongOpt(OPTION_PORT)
                .withDescription(OPTION_PORT_HELP)
                .withType(Integer.class)
                .hasArg()
                .withArgName(OPTION_PORT)
                .create());

        CommandLine cmdLine;
        try {
            cmdLine = cmdLineParser.parse(cmdOptions, options);

            if (!cmdLine.hasOption(OPTION_URL)) {
                throw new ConfigException(CONFIGURATION_MISSING_OPTION);
            }
        } catch (ParseException e) {
            throw new ConfigException(CONFIGURATION_UNABLE_TO_PARSE, e);
        }

        URL proxyUrl;
        try {
            proxyUrl = (URL) cmdLine.getParsedOptionValue(OPTION_URL);
        } catch (ParseException e) {
            logger.error("Unable to parse {} option : {} for value {}", OPTION_URL,
                    e.getMessage(), cmdLine.getOptionValue(OPTION_URL));
            throw new ConfigException(CONFIGURATION_WRONG_URL_VALUE, e);
        }

        Integer port = DEFAULT_LISTEN_PORT;
        try {
            if (cmdLine.hasOption(OPTION_PORT)) {
                port = (Integer) cmdLine.getParsedOptionValue(OPTION_PORT);
            }
        } catch (ParseException e) {
            logger.warn("Unable to parse {} option : {} - using default value {}", OPTION_PORT,
                    e.getMessage(), DEFAULT_LISTEN_PORT);
        }

        if (proxyUrl != null) {
            lConf = new ConfigBean(proxyUrl, port);
        }

        return lConf;
    }
}
