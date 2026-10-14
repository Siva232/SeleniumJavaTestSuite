package com.demoproject.utils;

import com.demoproject.exceptions.FrameworkException;
import com.demoproject.reporting.ReportContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Captures browser screenshots and stores them in the run's {@code screenshots/} folder.
 */
public final class ScreenshotUtils {

    private static final Logger LOG = Logger.getLogger(ScreenshotUtils.class.getName());

    private ScreenshotUtils() {
    }

    /**
     * @param driver browser to capture
     * @return PNG bytes, or empty if the browser cannot be captured (e.g. an alert is open)
     */
    public static Optional<byte[]> capture(WebDriver driver) {
        try {
            return Optional.of(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        } catch (WebDriverException e) {
            LOG.warning(() -> "Screenshot not captured: " + e.getMessage().lines().findFirst().orElse(""));
            return Optional.empty();
        }
    }

    /**
     * @param png      PNG bytes
     * @param baseName descriptive name (sanitised)
     * @return the written file
     */
    public static Path save(byte[] png, String baseName) {
        Path file = ReportContext.getInstance().getScreenshotsDir()
                .resolve(FileHelper.sanitize(baseName) + "_" + FileHelper.fileTimestamp() + ".png");
        try {
            Files.write(file, png);
            return file;
        } catch (IOException e) {
            throw new FrameworkException("Unable to save screenshot " + file, e);
        }
    }
}
