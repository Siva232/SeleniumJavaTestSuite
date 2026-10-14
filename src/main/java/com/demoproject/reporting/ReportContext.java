package com.demoproject.reporting;

import com.demoproject.config.ConfigReader;
import com.demoproject.exceptions.FrameworkException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Paths of the current run's report folder: {@code <report.dir>/Run_yyyyMMdd_HHmmss/} with
 * {@code index.html}, {@code screenshots/}, {@code videos/} and {@code logs/}. One per JVM.
 */
public final class ReportContext {

    /** Prefix of every run folder; used by {@link ReportHistoryCleaner}. */
    public static final String RUN_PREFIX = "Run_";
    private static final DateTimeFormatter RUN_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static volatile ReportContext instance;

    private final Path reportRoot;
    private final Path runDir;
    private final Path screenshotsDir;
    private final Path videosDir;
    private final Path logsDir;

    private ReportContext() {
        reportRoot = Paths.get(ConfigReader.getInstance().getReportDir()).toAbsolutePath().normalize();
        runDir = reportRoot.resolve(RUN_PREFIX + LocalDateTime.now().format(RUN_FORMAT));
        screenshotsDir = runDir.resolve("screenshots");
        videosDir = runDir.resolve("videos");
        logsDir = runDir.resolve("logs");
        try {
            Files.createDirectories(screenshotsDir);
            Files.createDirectories(videosDir);
            Files.createDirectories(logsDir);
        } catch (IOException e) {
            throw new FrameworkException("Unable to create report folders under " + runDir, e);
        }
    }

    /**
     * @return the shared instance; creates the run folder on first call
     */
    public static ReportContext getInstance() {
        ReportContext local = instance;
        if (local == null) {
            synchronized (ReportContext.class) {
                local = instance;
                if (local == null) {
                    local = new ReportContext();
                    instance = local;
                }
            }
        }
        return local;
    }

    /** @return folder containing all Run_* folders */
    public Path getReportRoot() {
        return reportRoot;
    }

    /** @return this run's folder */
    public Path getRunDir() {
        return runDir;
    }

    /** @return this run's Extent report file */
    public Path getReportFile() {
        return runDir.resolve("index.html");
    }

    /** @return screenshots folder */
    public Path getScreenshotsDir() {
        return screenshotsDir;
    }

    /** @return videos folder */
    public Path getVideosDir() {
        return videosDir;
    }

    /** @return logs folder */
    public Path getLogsDir() {
        return logsDir;
    }

    /**
     * @param file a file inside the run folder
     * @return path relative to index.html, with forward slashes, for embedding in the report
     */
    public String relativeToRunDir(Path file) {
        return runDir.relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }
}
