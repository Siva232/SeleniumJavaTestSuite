package com.demoproject.core;

import com.demoproject.config.ConfigReader;
import com.demoproject.logging.LoggingConfigurator;
import com.demoproject.reporting.ExtentManager;
import com.demoproject.reporting.ReportContext;
import com.demoproject.reporting.ReportHistoryCleaner;

import java.util.logging.Logger;

/**
 * One-time, idempotent start-up: validates configuration, creates the run's report folder,
 * configures logging, prunes old reports and initialises Extent.
 */
public final class FrameworkBootstrap {

    private static final Logger LOG = Logger.getLogger(FrameworkBootstrap.class.getName());
    private static volatile boolean initialized;

    private FrameworkBootstrap() {
    }

    /**
     * Safe to call from every scenario; only the first call does any work.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        synchronized (FrameworkBootstrap.class) {
            if (initialized) {
                return;
            }
            ConfigReader config = ConfigReader.getInstance();
            ReportContext context = ReportContext.getInstance();
            LoggingConfigurator.configure(context.getLogsDir().resolve("execution.log"), config.getLogLevel());
            ReportHistoryCleaner.clean(context.getReportRoot(), config.getReportHistoryCount());
            ExtentManager.getInstance();
            LOG.info(() -> String.format("DemoProject started: env=%s, browser=%s, headless=%s, threads=%d, "
                            + "rerun=%d, screenshots=%s, video=%s, report=%s",
                    config.getEnv(), config.getBrowser(), config.isHeadless(), config.getParallelThreadCount(),
                    config.getRerunCount(), config.getScreenshotMode(), config.getVideoMode(),
                    context.getReportFile()));
            initialized = true;
        }
    }
}
