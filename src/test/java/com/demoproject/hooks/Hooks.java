package com.demoproject.hooks;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.demoproject.config.CaptureMode;
import com.demoproject.config.ConfigReader;
import com.demoproject.context.ScenarioContext;
import com.demoproject.core.FrameworkBootstrap;
import com.demoproject.db.DbUtils;
import com.demoproject.driver.DriverManager;
import com.demoproject.reporting.ExtentTestManager;
import com.demoproject.reporting.HtmlEscaper;
import com.demoproject.reporting.ReportContext;
import com.demoproject.reporting.ScenarioAttemptTracker;
import com.demoproject.utils.ScreenshotUtils;
import com.demoproject.video.VideoRecorderManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Scenario lifecycle. Browser, screenshots and video apply only to scenarios tagged {@code @ui};
 * API/DB scenarios never open a browser.
 * <p>
 * Order: Before hooks run low-to-high, After hooks high-to-low, so the report node is created
 * first and closed last.
 */
public class Hooks {

    private static final Logger LOG = Logger.getLogger(Hooks.class.getName());
    private static final ThreadLocal<Boolean> FAILURE_SCREENSHOT_TAKEN = ThreadLocal.withInitial(() -> false);

    private final ConfigReader config = ConfigReader.getInstance();

    /**
     * Creates the Extent node (replacing the node of a previous failed attempt).
     *
     * @param scenario current scenario
     */
    @Before(order = 0)
    public void startScenario(Scenario scenario) {
        FrameworkBootstrap.initialize();
        int attempt = ScenarioAttemptTracker.nextAttempt(scenarioKey(scenario));
        ExtentTestManager.startTest(scenarioKey(scenario), scenario.getName(), scenario.getSourceTagNames(),
                attempt, ScenarioAttemptTracker.maxAttempts());
        LOG.info(() -> "START scenario '" + scenario.getName() + "' (attempt " + attempt + ") " + scenario.getUri());
    }

    /** Starts this thread's browser. */
    @Before(value = "@ui", order = 1)
    public void startBrowser() {
        DriverManager.initDriver();
        ExtentTestManager.getTest().assignDevice(config.getBrowser().name().toLowerCase());
    }

    /** Starts recording this thread's browser. */
    @Before(value = "@ui", order = 2)
    public void startRecording() {
        VideoRecorderManager.start();
    }

    /**
     * Screenshots per {@code screenshot.mode}: every step for ALL, the first failing step for FAILED.
     *
     * @param scenario current scenario
     */
    @AfterStep("@ui")
    public void afterStep(Scenario scenario) {
        CaptureMode mode = config.getScreenshotMode();
        boolean failed = scenario.isFailed();
        if (mode == CaptureMode.ALL) {
            attachScreenshot(scenario, failed ? "Failure screenshot" : "Step screenshot", failed);
        } else if (mode == CaptureMode.FAILED && failed && !FAILURE_SCREENSHOT_TAKEN.get()) {
            attachScreenshot(scenario, "Failure screenshot", true);
        }
    }

    /**
     * Final failure screenshot (if none yet), stop video, quit browser - in that order.
     *
     * @param scenario current scenario
     */
    @After(value = "@ui", order = 20)
    public void stopBrowser(Scenario scenario) {
        try {
            if (scenario.isFailed() && config.getScreenshotMode() != CaptureMode.NONE
                    && !FAILURE_SCREENSHOT_TAKEN.get()) {
                attachScreenshot(scenario, "Failure screenshot", true);
            }
        } finally {
            try {
                // Browsers cannot play AVI inline, so the report links to the file instead of embedding it.
                VideoRecorderManager.stop(scenario.getName(), scenario.isFailed()).ifPresent(avi ->
                        ExtentTestManager.currentTest().ifPresent(test -> test.info(
                                "Scenario recording: <a href=\"" + HtmlEscaper.escape(avi) + "\" target=\"_blank\">"
                                        + HtmlEscaper.escape(avi.substring(avi.lastIndexOf('/') + 1)) + "</a>")));
            } finally {
                DriverManager.quitDriver();
                FAILURE_SCREENSHOT_TAKEN.remove();
            }
        }
    }

    /** Closes this thread's DB connection if a step opened one. */
    @After(order = 10)
    public void closeDatabase() {
        DbUtils.closeConnection();
    }

    /**
     * Writes the final outcome to the report and clears scenario state.
     *
     * @param scenario current scenario
     */
    @After(order = 0)
    public void finishScenario(Scenario scenario) {
        LOG.info(() -> "END scenario '" + scenario.getName() + "' -> " + scenario.getStatus());
        ExtentTestManager.endTest(scenario.isFailed(), scenario.getStatus().name(), ScenarioAttemptTracker.maxAttempts());
        ScenarioContext.clear();
    }

    private void attachScreenshot(Scenario scenario, String title, boolean failure) {
        if (!DriverManager.hasDriver()) {
            return;
        }
        ScreenshotUtils.capture(DriverManager.getDriver()).ifPresent(png -> {
            scenario.attach(png, "image/png", title);
            Path file = ScreenshotUtils.save(png, scenario.getName());
            String relative = ReportContext.getInstance().relativeToRunDir(file);
            ExtentTest test = ExtentTestManager.getTest();
            if (failure) {
                test.fail(title, MediaEntityBuilder.createScreenCaptureFromPath(relative, title).build());
                FAILURE_SCREENSHOT_TAKEN.set(true);
            } else {
                test.info(title, MediaEntityBuilder.createScreenCaptureFromPath(relative, title).build());
            }
        });
    }

    private static String scenarioKey(Scenario scenario) {
        return scenario.getUri() + ":" + scenario.getLine();
    }
}
