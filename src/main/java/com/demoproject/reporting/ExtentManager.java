package com.demoproject.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.demoproject.config.ConfigReader;

/**
 * Owns the single {@link ExtentReports} instance for the run (Spark reporter, system info).
 * Mutations of the shared report model are serialised on {@link #lock()}.
 */
public final class ExtentManager {

    private static final Object LOCK = new Object();
    private static volatile ExtentReports extent;

    private ExtentManager() {
    }

    /**
     * @return the shared report, created on first use
     */
    public static ExtentReports getInstance() {
        ExtentReports local = extent;
        if (local == null) {
            synchronized (LOCK) {
                local = extent;
                if (local == null) {
                    local = create();
                    extent = local;
                }
            }
        }
        return local;
    }

    /**
     * @return monitor guarding create/remove/flush on the shared report
     */
    public static Object lock() {
        return LOCK;
    }

    /**
     * Writes the report to disk. Safe to call more than once.
     */
    public static void flush() {
        synchronized (LOCK) {
            if (extent != null) {
                extent.flush();
            }
        }
    }

    private static ExtentReports create() {
        ConfigReader config = ConfigReader.getInstance();
        ReportContext context = ReportContext.getInstance();

        ExtentSparkReporter spark = new ExtentSparkReporter(context.getReportFile().toFile());
        spark.config().setDocumentTitle("DemoProject Automation Report");
        spark.config().setReportName("DemoProject - " + config.getEnv() + " execution");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setEncoding("UTF-8");
        spark.config().setTimeStampFormat("dd MMM yyyy HH:mm:ss");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);
        reports.setSystemInfo("Environment", config.getEnv());
        reports.setSystemInfo("Browser", config.getBrowser().name() + (config.isHeadless() ? " (headless)" : ""));
        reports.setSystemInfo("OS", System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");
        reports.setSystemInfo("Java Version", System.getProperty("java.version"));
        reports.setSystemInfo("Thread Count", String.valueOf(config.getParallelThreadCount()));
        reports.setSystemInfo("Rerun Count", String.valueOf(config.getRerunCount()));
        reports.setSystemInfo("Application URL", config.getAppUrl());
        reports.setSystemInfo("API Base URL", config.getApiBaseUrl());
        return reports;
    }
}
