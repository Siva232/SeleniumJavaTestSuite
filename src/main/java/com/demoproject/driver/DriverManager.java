package com.demoproject.driver;

import com.demoproject.config.ConfigReader;
import com.demoproject.exceptions.FrameworkException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.util.logging.Logger;

/**
 * Holds one {@link WebDriver} per thread so parallel scenarios never share a browser.
 */
public final class DriverManager {

    private static final Logger LOG = Logger.getLogger(DriverManager.class.getName());
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    /**
     * Starts the configured browser for the current thread (quits any leftover one first).
     */
    public static void initDriver() {
        quitDriver();
        ConfigReader config = ConfigReader.getInstance();
        DRIVER.set(DriverFactory.createDriver(config.getBrowser(), config.isHeadless()));
    }

    /**
     * @return the current thread's driver
     * @throws FrameworkException if no browser was started on this thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException("No WebDriver on thread " + Thread.currentThread().getName()
                    + ". UI scenarios must be tagged @ui so Hooks start a browser.");
        }
        return driver;
    }

    /**
     * @return true if a browser is running on the current thread
     */
    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    /**
     * Quits and unbinds the current thread's driver; errors are logged, not thrown.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
            LOG.info("Browser closed");
        } catch (WebDriverException e) {
            LOG.warning(() -> "Error while quitting browser: " + e.getMessage());
        } finally {
            DRIVER.remove();
        }
    }
}
