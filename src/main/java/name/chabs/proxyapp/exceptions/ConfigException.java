package name.chabs.proxyapp.exceptions;

/**
 * Exception throwed when error occurs during configuration loading
 */
public class ConfigException extends Exception {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
