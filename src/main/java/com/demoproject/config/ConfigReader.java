package com.demoproject.config;

import com.demoproject.driver.BrowserType;
import com.demoproject.exceptions.FrameworkException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

/**
 * Thread-safe singleton giving typed access to {@code config/config.properties}.
 * <p>
 * Resolution order for every key: {@code -Dkey=value} &gt; {@code <ENV>.key} &gt; {@code key}.
 * {@code ENC(...)} values are decrypted on access (never at load time, never logged), so
 * suites that do not need a secret can run without the decryption key.
 * Mandatory keys and value formats are validated once, at construction.
 */
public final class ConfigReader {

    private static final String CONFIG_FILE = "config/config.properties";
    private static final Set<String> SUPPORTED_ENVS = Set.of("QA", "UAT", "PROD");
    private static final int DEFAULT_API_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_VIDEO_MAX_WIDTH = 1280;

    private static volatile ConfigReader instance;

    private final LayeredProperties properties;
    private final String env;

    private ConfigReader() {
        this.properties = LayeredProperties.fromClasspath(CONFIG_FILE);
        this.env = resolveEnv();
        validate();
    }

    /**
     * @return the shared instance, created and validated on first use
     * @throws FrameworkException if the configuration is missing or invalid
     */
    public static ConfigReader getInstance() {
        ConfigReader local = instance;
        if (local == null) {
            synchronized (ConfigReader.class) {
                local = instance;
                if (local == null) {
                    local = new ConfigReader();
                    instance = local;
                }
            }
        }
        return local;
    }

    // ------------------------------------------------------------------ generic getters

    /**
     * @param key configuration key
     * @return the resolved, decrypted, non-blank value
     * @throws FrameworkException if the key is not defined
     */
    public String getString(String key) {
        return getOptional(key).orElseThrow(() -> missingKey(key));
    }

    /**
     * @param key configuration key
     * @return the resolved, decrypted value, or empty if undefined or blank
     */
    public Optional<String> getOptional(String key) {
        return properties.lookup(key, env)
                .filter(v -> !v.isBlank())
                .map(v -> LayeredProperties.decryptIfEncrypted(v, key));
    }

    /**
     * @param key configuration key
     * @return the value parsed as an int
     */
    public int getInt(String key) {
        return parseInt(key, getString(key));
    }

    /**
     * @param key          configuration key
     * @param defaultValue value used when the key is not defined
     * @return the value parsed as an int, or the default
     */
    public int getInt(String key, int defaultValue) {
        return getOptional(key).map(v -> parseInt(key, v)).orElse(defaultValue);
    }

    /**
     * @param key configuration key
     * @return the value parsed strictly as {@code true}/{@code false}
     */
    public boolean getBoolean(String key) {
        String value = getString(key).toLowerCase(Locale.ROOT);
        if (!value.equals("true") && !value.equals("false")) {
            throw new FrameworkException("Invalid boolean '" + value + "' for '" + key + "' (env " + env
                    + "). Use true or false.");
        }
        return Boolean.parseBoolean(value);
    }

    // ------------------------------------------------------------------ typed getters

    /** @return active environment, e.g. QA */
    public String getEnv() {
        return env;
    }

    /** @return URL of the automation practice page */
    public String getAppUrl() {
        return getString(ConfigKeys.APP_URL);
    }

    /** @return URL of the login practice page (falls back to {@link #getAppUrl()}) */
    public String getLoginUrl() {
        return getOptional(ConfigKeys.APP_LOGIN_URL).orElseGet(this::getAppUrl);
    }

    /** @return default application username */
    public String getAppUsername() {
        return getString(ConfigKeys.APP_USERNAME);
    }

    /** @return decrypted default application password (never log it) */
    public String getAppPassword() {
        return getString(ConfigKeys.APP_PASSWORD);
    }

    /** @return REST API base URI */
    public String getApiBaseUrl() {
        return getString(ConfigKeys.API_BASE_URL);
    }

    /** @return REST connection/socket timeout in seconds */
    public int getApiTimeoutSeconds() {
        return getInt(ConfigKeys.API_TIMEOUT_SECONDS, DEFAULT_API_TIMEOUT_SECONDS);
    }

    /** @return optional bearer token for the API (decrypted) */
    public Optional<String> getApiAuthToken() {
        return getOptional(ConfigKeys.API_AUTH_TOKEN);
    }

    /** @return JDBC URL */
    public String getDbUrl() {
        return getString(ConfigKeys.DB_URL);
    }

    /** @return database user */
    public String getDbUsername() {
        return getString(ConfigKeys.DB_USERNAME);
    }

    /** @return decrypted database password (never log it) */
    public String getDbPassword() {
        return getString(ConfigKeys.DB_PASSWORD);
    }

    /** @return JDBC connect and query timeout in seconds */
    public int getDbTimeoutSeconds() {
        return getInt(ConfigKeys.DB_TIMEOUT_SECONDS);
    }

    /** @return browser to launch */
    public BrowserType getBrowser() {
        return BrowserType.from(getString(ConfigKeys.BROWSER));
    }

    /** @return whether the browser runs headless */
    public boolean isHeadless() {
        return getBoolean(ConfigKeys.HEADLESS);
    }

    /** @return explicit wait timeout in seconds */
    public int getExplicitTimeoutSeconds() {
        return getInt(ConfigKeys.EXPLICIT_TIMEOUT_SECONDS);
    }

    /** @return page-load timeout in seconds */
    public int getPageLoadTimeoutSeconds() {
        return getInt(ConfigKeys.PAGE_LOAD_TIMEOUT_SECONDS);
    }

    /** @return number of scenarios executed in parallel */
    public int getParallelThreadCount() {
        return getInt(ConfigKeys.PARALLEL_THREAD_COUNT);
    }

    /** @return extra attempts for a failed scenario (0 disables retry) */
    public int getRerunCount() {
        return getInt(ConfigKeys.RERUN_COUNT);
    }

    /** @return when screenshots are captured */
    public CaptureMode getScreenshotMode() {
        return CaptureMode.from(getString(ConfigKeys.SCREENSHOT_MODE), ConfigKeys.SCREENSHOT_MODE);
    }

    /** @return when recordings are kept */
    public CaptureMode getVideoMode() {
        return CaptureMode.from(getString(ConfigKeys.VIDEO_RECORDING), ConfigKeys.VIDEO_RECORDING);
    }

    /** @return milliseconds between captured video frames */
    public int getVideoFrameIntervalMs() {
        return getInt(ConfigKeys.VIDEO_FRAME_INTERVAL_MS);
    }

    /** @return maximum width (px) of recorded frames */
    public int getVideoMaxWidth() {
        return getInt(ConfigKeys.VIDEO_MAX_WIDTH, DEFAULT_VIDEO_MAX_WIDTH);
    }

    /** @return root folder holding the Run_* report folders */
    public String getReportDir() {
        return getString(ConfigKeys.REPORT_DIR);
    }

    /** @return number of Run_* folders to keep */
    public int getReportHistoryCount() {
        return getInt(ConfigKeys.REPORT_HISTORY_COUNT);
    }

    /** @return JUL log level for framework loggers (default INFO) */
    public Level getLogLevel() {
        String value = getOptional(ConfigKeys.LOG_LEVEL).orElse("INFO");
        try {
            return Level.parse(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new FrameworkException("Invalid log level '" + value + "' for '" + ConfigKeys.LOG_LEVEL + "'");
        }
    }

    // ------------------------------------------------------------------ internals

    private String resolveEnv() {
        String value = properties.lookup(ConfigKeys.ENV, null)
                .filter(v -> !v.isBlank())
                .orElseThrow(() -> new FrameworkException("Mandatory key 'env' is missing in " + CONFIG_FILE))
                .toUpperCase(Locale.ROOT);
        if (!SUPPORTED_ENVS.contains(value)) {
            throw new FrameworkException("Unsupported env '" + value + "'. Allowed: " + SUPPORTED_ENVS);
        }
        return value;
    }

    /** Fails fast, listing every problem at once instead of one per run. */
    private void validate() {
        List<String> errors = new ArrayList<>();
        for (String key : ConfigKeys.MANDATORY) {
            if (properties.lookup(key, env).filter(v -> !v.isBlank()).isEmpty()) {
                errors.add("missing mandatory key '" + key + "'");
            }
        }
        if (errors.isEmpty()) {
            List<Runnable> typedChecks = List.of(
                    this::getBrowser, this::isHeadless, this::getScreenshotMode, this::getVideoMode,
                    () -> requirePositive(ConfigKeys.EXPLICIT_TIMEOUT_SECONDS, getExplicitTimeoutSeconds()),
                    () -> requirePositive(ConfigKeys.PAGE_LOAD_TIMEOUT_SECONDS, getPageLoadTimeoutSeconds()),
                    () -> requirePositive(ConfigKeys.PARALLEL_THREAD_COUNT, getParallelThreadCount()),
                    () -> requirePositive(ConfigKeys.DB_TIMEOUT_SECONDS, getDbTimeoutSeconds()),
                    () -> requirePositive(ConfigKeys.VIDEO_FRAME_INTERVAL_MS, getVideoFrameIntervalMs()),
                    () -> requirePositive(ConfigKeys.REPORT_HISTORY_COUNT, getReportHistoryCount()),
                    () -> requirePositive(ConfigKeys.VIDEO_MAX_WIDTH, getVideoMaxWidth()),
                    () -> requireNonNegative(ConfigKeys.RERUN_COUNT, getRerunCount()),
                    this::getLogLevel);
            for (Runnable check : typedChecks) {
                try {
                    check.run();
                } catch (FrameworkException e) {
                    errors.add(e.getMessage());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new FrameworkException("Invalid configuration in " + CONFIG_FILE + " for env '" + env + "':\n - "
                    + String.join("\n - ", errors));
        }
    }

    private static void requirePositive(String key, int value) {
        if (value <= 0) {
            throw new FrameworkException("'" + key + "' must be greater than 0 but was " + value);
        }
    }

    private static void requireNonNegative(String key, int value) {
        if (value < 0) {
            throw new FrameworkException("'" + key + "' must be 0 or greater but was " + value);
        }
    }

    private int parseInt(String key, String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new FrameworkException("Invalid integer '" + value + "' for '" + key + "' (env " + env + ")");
        }
    }

    private FrameworkException missingKey(String key) {
        return new FrameworkException("Configuration key '" + key + "' is not defined for env '" + env
                + "'. Checked: -D" + key + ", " + env + "." + key + ", " + key + " in " + CONFIG_FILE);
    }
}
