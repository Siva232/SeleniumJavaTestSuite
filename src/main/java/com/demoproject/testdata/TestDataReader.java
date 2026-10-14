package com.demoproject.testdata;

import com.demoproject.config.ConfigReader;
import com.demoproject.config.LayeredProperties;
import com.demoproject.exceptions.FrameworkException;
import com.demoproject.utils.PlaceholderResolver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Thread-safe singleton for {@code testdata/testdata.properties}, independent of {@link ConfigReader}.
 * <p>
 * Lookup precedence: {@code -Dkey=value} &gt; {@code <ENV>.key} &gt; {@code key}. Values may be
 * {@code ENC(...)} (decrypted on access, never logged) and may contain placeholders such as
 * {@code ${timestamp}}, {@code ${random.alpha:N}}, {@code ${random.numeric:N}}, {@code ${uuid}} or
 * {@code ${other.key}}. Placeholders are evaluated each time a key is read.
 */
public final class TestDataReader {

    private static final String TEST_DATA_FILE = "testdata/testdata.properties";

    private static volatile TestDataReader instance;

    private final LayeredProperties properties;
    private final String env;
    /** Keys currently being resolved on this thread, to detect ${a} -> ${b} -> ${a} cycles. */
    private final ThreadLocal<Deque<String>> resolving = ThreadLocal.withInitial(ArrayDeque::new);

    private TestDataReader() {
        this.properties = LayeredProperties.fromClasspath(TEST_DATA_FILE);
        this.env = ConfigReader.getInstance().getEnv();
    }

    /**
     * @return the shared instance
     */
    public static TestDataReader getInstance() {
        TestDataReader local = instance;
        if (local == null) {
            synchronized (TestDataReader.class) {
                local = instance;
                if (local == null) {
                    local = new TestDataReader();
                    instance = local;
                }
            }
        }
        return local;
    }

    /**
     * @param key test data key, e.g. {@code login.valid.username}
     * @return the decrypted, placeholder-resolved value (may be empty if defined as empty)
     * @throws FrameworkException naming the key and env if the key is not defined
     */
    public String get(String key) {
        String raw = properties.lookup(key, env).orElseThrow(() -> new FrameworkException(
                "Test data key '" + key + "' is not defined for env '" + env + "'. Checked: -D" + key + ", "
                        + env + "." + key + ", " + key + " in " + TEST_DATA_FILE));
        Deque<String> stack = resolving.get();
        if (stack.contains(key)) {
            throw new FrameworkException("Circular placeholder reference in " + TEST_DATA_FILE + ": "
                    + String.join(" -> ", stack) + " -> " + key);
        }
        stack.push(key);
        try {
            String decrypted = LayeredProperties.decryptIfEncrypted(raw, key);
            return resolvePlaceholders(decrypted);
        } finally {
            stack.pop();
        }
    }

    /**
     * @param key test data key
     * @return the value if the key is defined at any level
     */
    public Optional<String> find(String key) {
        return hasKey(key) ? Optional.of(get(key)) : Optional.empty();
    }

    /**
     * @param key test data key
     * @return true if the key is defined (system property, env-prefixed or default)
     */
    public boolean hasKey(String key) {
        return key != null && properties.lookup(key, env).isPresent();
    }

    /**
     * Resolves a value coming from a feature file: if it is a known key its value is returned,
     * otherwise it is treated as a literal (placeholders inside the literal are still resolved).
     *
     * @param keyOrLiteral key or literal text
     * @return resolved value
     */
    public String resolve(String keyOrLiteral) {
        if (keyOrLiteral == null) {
            return null;
        }
        return hasKey(keyOrLiteral) ? get(keyOrLiteral) : resolvePlaceholders(keyOrLiteral);
    }

    /**
     * @param keyOrLiteral key or literal number
     * @return resolved value parsed as an int
     */
    public int resolveInt(String keyOrLiteral) {
        String value = resolve(keyOrLiteral);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new FrameworkException("Test data '" + keyOrLiteral + "' (env " + env + ") resolved to '" + value
                    + "', which is not an integer");
        }
    }

    /**
     * @param text text containing {@code ${...}} placeholders
     * @return resolved text
     */
    public String resolvePlaceholders(String text) {
        return resolvePlaceholders(text, UnaryOperator.identity());
    }

    /**
     * @param text    text containing {@code ${...}} placeholders
     * @param encoder applied to each substituted value (e.g. JSON escaping)
     * @return resolved text
     */
    public String resolvePlaceholders(String text, UnaryOperator<String> encoder) {
        return PlaceholderResolver.resolve(text, this::get, encoder);
    }

    /**
     * @return environment the data is resolved for
     */
    public String getEnv() {
        return env;
    }
}
