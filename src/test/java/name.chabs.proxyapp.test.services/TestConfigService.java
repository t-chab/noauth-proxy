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

    private static final int BIND_PORT_TEST = 58555;
    private static final String URL_TEST = "http://login:password@some.proxy.url:8000";
    private static final String LS = System.getProperty("line.separator");

    @Test
    public void testLoadConfigFromFile() {
        ConfigService configService = new ConfigServiceImpl();
        try {
            final File file = File.createTempFile(ConfigService.CONFIG_FILE_NAME, ConfigService.CONFIG_FILE_SUFFIX);
            Files.write(Paths.get(file.getPath()), (ConfigService.CONF_PROXY_APP_PORT + '=' + BIND_PORT_TEST + LS + ConfigService.CONF_PROXY_APP_URL + '=' + URL_TEST + LS).getBytes());
            ConfigBean config = configService.loadConfiguration(file);
            Assert.assertEquals(URL_TEST, config.getProxyUrl().toString());
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
        final String[] configData = new String[]{"--" + ConfigService.OPTION_URL + '=' + URL_TEST,
                "--" + ConfigService.OPTION_PORT + '=' + BIND_PORT_TEST};
        try {
            ConfigBean config = configService.loadConfiguration(configData);
            Assert.assertEquals(URL_TEST, config.getProxyUrl().toString());
            Assert.assertEquals(BIND_PORT_TEST, config.getProxyPort());
        } catch (ConfigException e) {
            Assert.fail("Unable to load configuration from sample command line.", e);
        }
    }
}
