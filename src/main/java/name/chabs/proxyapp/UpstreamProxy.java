package name.chabs.proxyapp;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Extends {@link ChainedProxyAdapter} to set up a custom forward proxy
 * with basic http auth credentials handling
 *
 * @see org.littleshoot.proxy.ChainedProxyAdapter
 */
class UpstreamProxy extends ChainedProxyAdapter {

    private final static String ENCODE_CHARSET = "UTF-8";
    private final static Logger logger = LoggerFactory.getLogger(UpstreamProxy.class);
    private String proxyHostname;
    private int proxyPort;
    private String proxyLogin;
    private String proxyPassword;

    /**
     * Parses proxyUrl to initialize proxy credentials, hostname and port.
     *
     * @param proxyUrl : {@link URL} of the remote proxy to use.
     *                 Throws {@link IllegalArgumentException} if <em>proxyUrl</em> is null;
     */
    public UpstreamProxy(final URL proxyUrl) {
        if (proxyUrl == null) {
            throw new IllegalArgumentException();
        }
        proxyHostname = proxyUrl.getHost();
        logger.debug("Using proxy host : {}", proxyHostname);
        proxyPort = proxyUrl.getPort();
        logger.debug("Using proxy port : {}", proxyPort);
        final String userInfo = proxyUrl.getUserInfo();
        if (userInfo != null) {
            String[] lCredentials = userInfo.split(":");
            proxyLogin = lCredentials[0];
            logger.debug("Using proxy username : {}", proxyLogin);
            proxyPassword = lCredentials[1];
            if (proxyPassword != null) {
                logger.debug("Using empty password : {}", (proxyPassword.length() == 0));
            }
        }
    }

    /**
     * @return Proxy credentials
     */
    private String getCredentials() {
        return proxyLogin + ':' + proxyPassword;
    }

    /**
     * @return Base64 encoded proxy credentials
     */
    private String getEncodedCredentials() {
        return Base64.getEncoder().encodeToString(getCredentials().getBytes(Charset.forName(ENCODE_CHARSET)));
    }

    @Override
    public InetSocketAddress getChainedProxyAddress() {
        try {
            return new InetSocketAddress(InetAddress.getByName(proxyHostname), proxyPort);
        } catch (UnknownHostException uhe) {
            throw new RuntimeException(
                    "Unable to resolve " + proxyHostname);
        }
    }

    @Override
    public void filterRequest(HttpObject httpObject) {
        // Add credentials to each request before sending to remote proxy
        if (httpObject instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) httpObject;
            req.headers().add("Proxy-Authorization", "Basic " + getEncodedCredentials());
        }
    }
}
