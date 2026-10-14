package com.demoproject.video;

import com.demoproject.config.CaptureMode;
import com.demoproject.config.ConfigReader;
import com.demoproject.driver.DriverManager;
import com.demoproject.reporting.ReportContext;
import com.demoproject.utils.FileHelper;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Keeps one {@link VideoRecorder} per thread (= per scenario) and applies {@code video.recording}.
 * Never throws: recording problems are logged as warnings so they cannot fail a test.
 */
public final class VideoRecorderManager {

    private static final Logger LOG = Logger.getLogger(VideoRecorderManager.class.getName());
    private static final ThreadLocal<VideoRecorder> RECORDER = new ThreadLocal<>();

    private VideoRecorderManager() {
    }

    /**
     * Starts recording the current thread's browser, unless recording is disabled or there is
     * no browser (API/DB-only scenarios).
     */
    public static void start() {
        try {
            ConfigReader config = ConfigReader.getInstance();
            if (config.getVideoMode() == CaptureMode.NONE || !DriverManager.hasDriver()) {
                return;
            }
            VideoRecorder recorder = new VideoRecorder(DriverManager.getDriver(),
                    config.getVideoFrameIntervalMs(), config.getVideoMaxWidth());
            recorder.start();
            RECORDER.set(recorder);
        } catch (Exception e) {
            LOG.warning(() -> "Video recording not started: " + e.getMessage());
        }
    }

    /**
     * Stops the current thread's recorder; keeps the GIF only if the outcome matches the mode.
     * Must be called before the browser is quit.
     *
     * @param scenarioName   used for the file name
     * @param scenarioFailed scenario outcome
     * @return path of the GIF relative to the report's index.html, if one was kept
     */
    public static Optional<String> stop(String scenarioName, boolean scenarioFailed) {
        VideoRecorder recorder = RECORDER.get();
        if (recorder == null) {
            return Optional.empty();
        }
        RECORDER.remove();
        try {
            boolean keep = ConfigReader.getInstance().getVideoMode().shouldKeep(scenarioFailed);
            ReportContext context = ReportContext.getInstance();
            Path target = keep
                    ? context.getVideosDir().resolve(FileHelper.sanitize(scenarioName) + "_"
                    + FileHelper.fileTimestamp() + ".gif")
                    : null;
            return recorder.stop(target) ? Optional.of(context.relativeToRunDir(target)) : Optional.empty();
        } catch (Exception e) {
            LOG.warning(() -> "Video recording not saved: " + e.getMessage());
            return Optional.empty();
        }
    }
}
