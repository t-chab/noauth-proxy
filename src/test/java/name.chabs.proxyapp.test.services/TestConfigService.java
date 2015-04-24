package name.chabs.proxyapp.test.services;

import name.chabs.proxyapp.beans.ConfigBean;
import name.chabs.proxyapp.exceptions.ConfigException;
import name.chabs.proxyapp.services.ConfigService;
import name.chabs.proxyapp.services.ConfigServiceImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test class for {@link ConfigService}
 *
 * @see ConfigService
 */
public class TestConfigService {

    private final static int BIND_PORT_TEST = 58555;
    private final static String URL_TEST = "http://login:password@some.proxy.url:8000";

    @Test
    public void testLoadConfigFromFile() {
        ConfigService configService = new ConfigServiceImpl();
        try {
            final File file = File.createTempFile(ConfigService.CONFIG_FILE_NAME, ConfigService.CONFIG_FILE_SUFFIX);
            final StringBuilder configuration = new StringBuilder(256);
            configuration.append(ConfigService.CONF_PROXY_APP_PORT).append('=').append(BIND_PORT_TEST);
            configuration.append(ConfigService.CONF_PROXY_APP_URL).append('=').append(URL_TEST);
            Files.write(Paths.get(file.getPath()), configuration.toString().getBytes());
            ConfigBean config = configService.loadConfiguration(file);
            Assert.assertEquals(URL_TEST, config.getProxyUrl());
            Assert.assertEquals(BIND_PORT_TEST, config.getProxyPort());
        } catch (IOException e) {
            Assert.fail("Unable to create sample configuration file.", e);
        } catch (ConfigException e) {
            Assert.fail("Unable to load configuration from sample file", e);
        }
    }

    @Test
    public void testLoadConfigFromArgs() {
        ConfigService configService = new ConfigServiceImpl();
        final String[] configData = new String[]{ConfigService.OPTION_URL + '=' + URL_TEST,
                ConfigService.OPTION_PORT + '=' + BIND_PORT_TEST};
        try {
            ConfigBean config = configService.loadConfiguration(configData);
            Assert.assertEquals(URL_TEST, config.getProxyUrl());
            Assert.assertEquals(BIND_PORT_TEST, config.getProxyPort());
        } catch (ConfigException e) {
            Assert.fail("Unable to load configuration from sample file", e);
        }
    }
}
