package com.demoproject.config;

import com.demoproject.exceptions.FrameworkException;
import com.demoproject.utils.EncryptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

/**
 * Immutable view over a classpath properties file that resolves keys with the framework's
 * precedence rules: {@code -Dkey=value} &gt; {@code <ENV>.key} &gt; {@code key}.
 * Shared by {@link ConfigReader} and {@code TestDataReader} so both follow identical rules.
 */
public final class LayeredProperties {

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    private final Properties properties;
    private final String source;

    private LayeredProperties(Properties properties, String source) {
        this.properties = properties;
        this.source = source;
    }

    /**
     * Loads a UTF-8 properties file from the classpath.
     *
     * @param resource classpath location, e.g. {@code config/config.properties}
     * @return loaded properties
     * @throws FrameworkException if the file is missing or unreadable
     */
    public static LayeredProperties fromClasspath(String resource) {
        Properties loaded = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = loader.getResourceAsStream(resource)) {
            if (in == null) {
                throw new FrameworkException("Properties file '" + resource + "' was not found on the classpath "
                        + "(expected under src/test/resources/" + resource + ")");
            }
            loaded.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new FrameworkException("Unable to read properties file '" + resource + "'", e);
        }
        return new LayeredProperties(loaded, resource);
    }

    /**
     * Resolves a raw (not decrypted) value. A blank system property is treated as "not set"
     * so that empty Maven defaults do not mask file values.
     *
     * @param key property key
     * @param env active environment (may be null for the {@code env} key itself)
     * @return trimmed raw value, if defined at any level
     */
    public Optional<String> lookup(String key, String env) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return Optional.of(systemValue.trim());
        }
        if (env != null && !env.isBlank()) {
            String scoped = properties.getProperty(env + "." + key);
            if (scoped != null) {
                return Optional.of(scoped.trim());
            }
        }
        return Optional.ofNullable(properties.getProperty(key)).map(String::trim);
    }

    /**
     * @return the classpath location this instance was loaded from
     */
    public String source() {
        return source;
    }

    /**
     * @param value raw value
     * @return true if the value is wrapped in {@code ENC(...)}
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUFFIX);
    }

    /**
     * Decrypts {@code ENC(...)} values; plain values are returned unchanged.
     *
     * @param value raw value
     * @param key   key name, used only in error messages (the value is never logged)
     * @return plain-text value
     */
    public static String decryptIfEncrypted(String value, String key) {
        if (!isEncrypted(value)) {
            return value;
        }
        String cipherText = value.substring(ENC_PREFIX.length(), value.length() - ENC_SUFFIX.length());
        try {
            return EncryptionUtils.decrypt(cipherText);
        } catch (FrameworkException e) {
            throw new FrameworkException("Unable to decrypt value of key '" + key + "': " + e.getMessage(), e);
        }
    }
}
