package com.demoproject.utils;

import com.demoproject.config.ConfigReader;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Explicit waits built on {@link WebDriverWait}. Every wait reports what it was waiting for
 * on timeout. There is intentionally no sleep-based wait.
 */
public class WaitUtils {

    private static final Duration POLLING = Duration.ofMillis(250);
    private static final String VISIBLE_TEXT_SCRIPT = "var e = arguments[0], s = window.getComputedStyle(e);"
            + "return (s.display !== 'none' && s.visibility !== 'hidden' && e.getClientRects().length > 0)"
            + " ? e.innerText : null;";

    private final WebDriver driver;
    private final Duration timeout;
    private final WebDriverWait wait;

    /**
     * @param driver driver to wait on, using {@code explicit.timeout.seconds}
     */
    public WaitUtils(WebDriver driver) {
        this(driver, Duration.ofSeconds(ConfigReader.getInstance().getExplicitTimeoutSeconds()));
    }

    /**
     * @param driver  driver to wait on
     * @param timeout maximum wait
     */
    public WaitUtils(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.timeout = timeout;
        this.wait = new WebDriverWait(driver, timeout, POLLING);
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /**
     * @param locator element locator
     * @return the element once visible
     */
    public WebElement visible(By locator) {
        return until(ExpectedConditions.visibilityOfElementLocated(locator), "visibility of " + locator);
    }

    /**
     * @param element element
     * @return the element once visible
     */
    public WebElement visible(WebElement element) {
        return until(ExpectedConditions.visibilityOf(element), "visibility of " + element);
    }

    /**
     * @param locator locator matching several elements
     * @return all matches once every one is visible
     */
    public List<WebElement> allVisible(By locator) {
        return until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator), "visibility of all " + locator);
    }

    /**
     * @param locator element locator
     * @return the element once present in the DOM (visible or not)
     */
    public WebElement present(By locator) {
        return until(ExpectedConditions.presenceOfElementLocated(locator), "presence of " + locator);
    }

    /**
     * @param locator element locator
     * @return the element once visible and enabled
     */
    public WebElement clickable(By locator) {
        return until(ExpectedConditions.elementToBeClickable(locator), "clickability of " + locator);
    }

    /**
     * @param locator element locator
     * @param text    expected substring
     * @return true once the element's text contains {@code text}
     */
    public boolean textPresent(By locator, String text) {
        return until(ExpectedConditions.textToBePresentInElementLocated(locator, text),
                "text '" + text + "' in " + locator);
    }

    /**
     * @param locator element locator
     * @return the element's visible text once it is non-empty
     */
    public String nonEmptyText(By locator) {
        return until(nonEmptyTextOf(locator), "non-empty text in " + locator);
    }

    /**
     * @param locator element locator
     * @return true once the element is hidden or removed
     */
    public boolean invisible(By locator) {
        return until(ExpectedConditions.invisibilityOfElementLocated(locator), "invisibility of " + locator);
    }

    /**
     * Verifies that an element does <em>not</em> become visible during the given period.
     * Uses polling rather than sleeping and returns as soon as the element appears.
     *
     * @param locator element locator
     * @param period  observation period
     * @return true if the element stayed hidden for the whole period
     */
    public boolean staysInvisible(By locator, Duration period) {
        try {
            new WebDriverWait(driver, period, POLLING)
                    .until(d -> isDisplayed(d, locator) ? Boolean.TRUE : null);
            return false;
        } catch (TimeoutException expected) {
            return true;
        }
    }

    /**
     * @param fragment expected URL substring
     * @return true once the current URL contains it
     */
    public boolean urlContains(String fragment) {
        return until(ExpectedConditions.urlContains(fragment), "URL containing '" + fragment + "'");
    }

    /**
     * @param count expected number of windows/tabs
     * @return true once that many windows are open
     */
    public boolean numberOfWindows(int count) {
        return until(ExpectedConditions.numberOfWindowsToBe(count), count + " open window(s)");
    }

    /**
     * @return the alert once present
     */
    public Alert alertPresent() {
        return until(ExpectedConditions.alertIsPresent(), "a JavaScript alert");
    }

    /**
     * @param frameLocator iframe locator
     * @return the driver, switched into the frame
     */
    public WebDriver frameAndSwitch(By frameLocator) {
        return until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator), "frame " + frameLocator);
    }

    /**
     * @return true once {@code document.readyState} is {@code complete}
     */
    public boolean pageLoaded() {
        return until(documentReady(), "document.readyState == complete");
    }

    /**
     * @param condition   any condition (including custom ones)
     * @param description what is being waited for, used in the timeout message
     * @param <T>         condition result type
     * @return the condition's non-null/true result
     */
    public <T> T until(ExpectedCondition<T> condition, String description) {
        try {
            return wait.until(condition);
        } catch (TimeoutException e) {
            throw new TimeoutException("Timed out after " + timeout.toSeconds() + "s waiting for " + description, e);
        }
    }

    // --------------------------------------------------------------- custom conditions

    /**
     * @return condition satisfied when the page has fully loaded
     */
    public static ExpectedCondition<Boolean> documentReady() {
        return d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState"));
    }

    /**
     * @param locator element locator
     * @return condition returning the element's text once visible and non-empty
     */
    public static ExpectedCondition<String> nonEmptyTextOf(By locator) {
        // Visibility and text are read in one script call so short-lived messages are not missed.
        return d -> {
            List<WebElement> elements = d.findElements(locator);
            if (elements.isEmpty()) {
                return null;
            }
            Object text = ((JavascriptExecutor) d).executeScript(VISIBLE_TEXT_SCRIPT, elements.get(0));
            return text == null || text.toString().isBlank() ? null : text.toString().trim();
        };
    }

    private static boolean isDisplayed(WebDriver d, By locator) {
        try {
            return d.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
