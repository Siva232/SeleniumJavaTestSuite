package com.demoproject.config;

import java.util.List;

/**
 * Names of every key read from {@code config/config.properties}.
 */
public final class ConfigKeys {

    public static final String ENV = "env";
    public static final String APP_URL = "app.url";
    public static final String APP_LOGIN_URL = "app.login.url";
    public static final String APP_USERNAME = "app.username";
    public static final String APP_PASSWORD = "app.password";
    public static final String API_BASE_URL = "api.base.url";
    public static final String API_TIMEOUT_SECONDS = "api.timeout.seconds";
    public static final String API_AUTH_TOKEN = "api.auth.token";
    public static final String DB_URL = "db.url";
    public static final String DB_USERNAME = "db.username";
    public static final String DB_PASSWORD = "db.password";
    public static final String DB_TIMEOUT_SECONDS = "db.timeout.seconds";
    public static final String BROWSER = "browser";
    public static final String HEADLESS = "headless";
    public static final String EXPLICIT_TIMEOUT_SECONDS = "explicit.timeout.seconds";
    public static final String PAGE_LOAD_TIMEOUT_SECONDS = "page.load.timeout.seconds";
    public static final String PARALLEL_THREAD_COUNT = "parallel.thread.count";
    public static final String RERUN_COUNT = "rerun.count";
    public static final String SCREENSHOT_MODE = "screenshot.mode";
    public static final String VIDEO_RECORDING = "video.recording";
    public static final String VIDEO_FRAME_INTERVAL_MS = "video.frame.interval.ms";
    public static final String VIDEO_MAX_WIDTH = "video.max.width";
    public static final String REPORT_DIR = "report.dir";
    public static final String REPORT_HISTORY_COUNT = "report.history.count";
    public static final String LOG_LEVEL = "log.level";

    /** Keys that must be present (non-blank) for the framework to start. */
    public static final List<String> MANDATORY = List.of(
            ENV, APP_URL, APP_USERNAME, APP_PASSWORD, API_BASE_URL,
            DB_URL, DB_USERNAME, DB_PASSWORD, DB_TIMEOUT_SECONDS,
            BROWSER, HEADLESS, EXPLICIT_TIMEOUT_SECONDS, PAGE_LOAD_TIMEOUT_SECONDS,
            PARALLEL_THREAD_COUNT, RERUN_COUNT, SCREENSHOT_MODE, VIDEO_RECORDING,
            VIDEO_FRAME_INTERVAL_MS, REPORT_DIR, REPORT_HISTORY_COUNT);

    private ConfigKeys() {
    }
}
