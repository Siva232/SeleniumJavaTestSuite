package com.demoproject.driver;

import com.demoproject.config.ConfigReader;
import com.demoproject.exceptions.FrameworkException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * Creates configured {@link WebDriver} instances. Driver binaries are resolved by Selenium Manager.
 * <p>
 * Unexpected alerts are set to IGNORE so a background video frame capture can never silently
 * dismiss an alert the test is about to assert on.
 */
public final class DriverFactory {

    private static final Logger LOG = Logger.getLogger(DriverFactory.class.getName());
    private static final String WINDOW_SIZE = "1920,1080";

    private DriverFactory() {
    }

    /**
     * @param browser  browser to launch
     * @param headless run without a visible window
     * @return a new driver with page-load and script timeouts applied
     */
    public static WebDriver createDriver(BrowserType browser, boolean headless) {
        ConfigReader config = ConfigReader.getInstance();
        WebDriver driver;
        try {
            driver = switch (browser) {
                case CHROME -> new ChromeDriver(chromiumOptions(new ChromeOptions(), headless));
                case EDGE -> new EdgeDriver(chromiumOptions(new EdgeOptions(), headless));
                case FIREFOX -> new FirefoxDriver(firefoxOptions(headless));
            };
        } catch (WebDriverException e) {
            throw new FrameworkException("Unable to start " + browser + " (headless=" + headless + "). Check that the "
                    + "browser is installed and Selenium Manager can download its driver: " + e.getMessage(), e);
        }
        Duration pageLoad = Duration.ofSeconds(config.getPageLoadTimeoutSeconds());
        driver.manage().timeouts().pageLoadTimeout(pageLoad);
        driver.manage().timeouts().scriptTimeout(pageLoad);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        if (!headless) {
            driver.manage().window().maximize();
        }
        LOG.info(() -> "Started " + browser + (headless ? " (headless)" : ""));
        return driver;
    }

    private static <T extends ChromiumOptions<T>> T chromiumOptions(T options, boolean headless) {
        options.addArguments("--window-size=" + WINDOW_SIZE, "--disable-notifications",
                "--disable-search-engine-choice-screen", "--no-first-run");
        if (headless) {
            options.addArguments("--headless=new", "--disable-gpu");
        }
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        if (headless) {
            options.addArguments("-headless");
        }
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
        return options;
    }
}
