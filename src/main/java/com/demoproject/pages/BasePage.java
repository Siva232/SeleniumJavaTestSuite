package com.demoproject.pages;

import com.demoproject.config.ConfigReader;
import com.demoproject.driver.DriverManager;
import com.demoproject.utils.WaitUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Base for all page objects: reusable, logged actions that always go through explicit waits.
 * Each page instance is bound to the current thread's driver.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WaitUtils wait;
    protected final ConfigReader config;
    protected final Logger log;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.wait = new WaitUtils(driver);
        this.config = ConfigReader.getInstance();
        this.log = Logger.getLogger(getClass().getName());
    }

    // ---------------------------------------------------------------- navigation

    /**
     * @param url page to open; waits for the document to finish loading
     */
    protected void navigateTo(String url) {
        log.info(() -> "Navigate to " + url);
        driver.get(url);
        wait.pageLoaded();
    }

    /** @return current page title */
    public String getTitle() {
        return driver.getTitle();
    }

    /** @return current URL */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ---------------------------------------------------------------- element actions

    /**
     * Waits for the element to be clickable and clicks it; falls back to a JavaScript click if
     * another element intercepts the click.
     *
     * @param locator element locator
     */
    protected void click(By locator) {
        log.info(() -> "Click " + locator);
        WebElement element = wait.clickable(locator);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            log.warning(() -> "Click intercepted on " + locator + ", retrying with JavaScript");
            jsClick(locator);
        }
    }

    /**
     * @param locator element to click through JavaScript
     */
    protected void jsClick(By locator) {
        log.info(() -> "JS click " + locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", wait.present(locator));
    }

    /**
     * @param locator input locator
     * @param text    text to type after clearing the field
     */
    protected void type(By locator, String text) {
        log.info(() -> "Type '" + text + "' into " + locator);
        enter(locator, text);
    }

    /**
     * Same as {@link #type(By, String)} but never logs the value.
     *
     * @param locator input locator
     * @param secret  value to type
     */
    protected void typeSecret(By locator, String secret) {
        log.info(() -> "Type ******** into " + locator);
        enter(locator, secret);
    }

    private void enter(By locator, String text) {
        WebElement element = wait.visible(locator);
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    /**
     * @param locator element locator
     * @param keys    keys to send without clearing, e.g. {@code Keys.ARROW_DOWN}
     */
    protected void sendKeys(By locator, CharSequence... keys) {
        log.info(() -> "Send keys to " + locator);
        wait.visible(locator).sendKeys(keys);
    }

    /**
     * @param locator element locator
     * @return trimmed visible text
     */
    protected String getText(By locator) {
        String text = wait.visible(locator).getText().trim();
        log.fine(() -> "Text of " + locator + " = '" + text + "'");
        return text;
    }

    /**
     * @param locator input locator
     * @return the input's current value property
     */
    protected String getValue(By locator) {
        String value = wait.present(locator).getDomProperty("value");
        return value == null ? "" : value;
    }

    /**
     * @param locator element locator
     * @return text of all matching elements (waits for at least one to be visible)
     */
    protected List<String> getTexts(By locator) {
        return wait.allVisible(locator).stream().map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    /**
     * @param locator element locator
     * @return all matches currently in the DOM, without waiting
     */
    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    /**
     * @param locator element locator
     * @return true if at least one match is currently displayed (no wait)
     */
    protected boolean isDisplayed(By locator) {
        return findAll(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * @param locator checkbox/radio locator
     * @return selection state
     */
    protected boolean isSelected(By locator) {
        return wait.present(locator).isSelected();
    }

    // ---------------------------------------------------------------- transient messages

    /**
     * Starts recording the text of an element that is only shown briefly (toast/banner). A
     * browser-side MutationObserver stores the text as soon as the element becomes visible, so the
     * message is not missed when WebDriver commands are slow (parallel runs, video capture).
     * Call before the action that triggers the message.
     *
     * @param cssSelector CSS selector of the message element
     */
    protected void watchTransientText(String cssSelector) {
        log.info(() -> "Watch transient text of " + cssSelector);
        ((JavascriptExecutor) driver).executeScript(
                "var sel = arguments[0], el = document.querySelector(sel);"
                        + "window.__transientText = window.__transientText || {};"
                        + "window.__transientText[sel] = null;"
                        + "if (!el) { return; }"
                        + "new MutationObserver(function () {"
                        + "  var s = window.getComputedStyle(el), t = el.innerText.trim();"
                        + "  if (s.display !== 'none' && s.visibility !== 'hidden' && t) { window.__transientText[sel] = t; }"
                        + "}).observe(el, {attributes: true, childList: true, subtree: true, characterData: true});",
                cssSelector);
    }

    /**
     * @param cssSelector selector previously passed to {@link #watchTransientText(String)}
     * @return the text the element showed after watching started
     */
    protected String waitForTransientText(String cssSelector) {
        return wait.until(d -> (String) ((JavascriptExecutor) d).executeScript(
                        "return window.__transientText ? window.__transientText[arguments[0]] : null;", cssSelector),
                "transient text in " + cssSelector);
    }

    // ---------------------------------------------------------------- select, scroll, hover

    /**
     * @param locator     select locator
     * @param visibleText option text
     */
    protected void selectByVisibleText(By locator, String visibleText) {
        log.info(() -> "Select '" + visibleText + "' in " + locator);
        new Select(wait.visible(locator)).selectByVisibleText(visibleText);
    }

    /**
     * @param locator select locator
     * @param value   option value attribute
     */
    protected void selectByValue(By locator, String value) {
        log.info(() -> "Select value '" + value + "' in " + locator);
        new Select(wait.visible(locator)).selectByValue(value);
    }

    /**
     * @param locator select locator
     * @param index   0-based option index
     */
    protected void selectByIndex(By locator, int index) {
        log.info(() -> "Select index " + index + " in " + locator);
        new Select(wait.visible(locator)).selectByIndex(index);
    }

    /**
     * @param locator select locator
     * @return text of the selected option
     */
    protected String getSelectedOption(By locator) {
        return new Select(wait.visible(locator)).getFirstSelectedOption().getText().trim();
    }

    /**
     * @param locator select locator
     * @return text of every option
     */
    protected List<String> getOptions(By locator) {
        return new Select(wait.visible(locator)).getOptions().stream()
                .map(o -> o.getText().trim()).collect(Collectors.toList());
    }

    /**
     * @param locator element to scroll to the centre of the viewport
     */
    protected void scrollIntoView(By locator) {
        log.info(() -> "Scroll to " + locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", wait.present(locator));
    }

    /**
     * @param locator element to move the mouse over
     */
    protected void hover(By locator) {
        log.info(() -> "Hover over " + locator);
        scrollIntoView(locator);
        new Actions(driver).moveToElement(wait.visible(locator)).perform();
    }

    // ---------------------------------------------------------------- frames

    /**
     * @param frameLocator iframe locator; waits until it is available and switches into it
     */
    protected void switchToFrame(By frameLocator) {
        log.info(() -> "Switch to frame " + frameLocator);
        wait.frameAndSwitch(frameLocator);
    }

    /**
     * @param nameOrId iframe name or id
     */
    protected void switchToFrame(String nameOrId) {
        log.info(() -> "Switch to frame '" + nameOrId + "'");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt(nameOrId),
                "frame '" + nameOrId + "'");
    }

    /** Switches back to the top-level document. */
    protected void switchToDefaultContent() {
        log.info("Switch to default content");
        driver.switchTo().defaultContent();
    }

    // ---------------------------------------------------------------- windows

    /** @return handle of the current window */
    protected String currentWindow() {
        return driver.getWindowHandle();
    }

    /** @return number of open windows/tabs */
    public int getWindowCount() {
        return driver.getWindowHandles().size();
    }

    /**
     * Waits for a window that is not in {@code existingHandles} and switches to it.
     *
     * @param existingHandles handles open before the window was triggered
     * @return the new window's handle
     */
    protected String switchToNewWindow(Set<String> existingHandles) {
        ExpectedCondition<String> newWindow = d -> {
            Set<String> handles = new HashSet<>(d.getWindowHandles());
            handles.removeAll(existingHandles);
            return handles.isEmpty() ? null : handles.iterator().next();
        };
        String handle = wait.until(newWindow, "a new browser window");
        log.info(() -> "Switch to new window " + handle);
        driver.switchTo().window(handle);
        return handle;
    }

    /**
     * @param handle window to switch to
     */
    protected void switchToWindow(String handle) {
        log.info(() -> "Switch to window " + handle);
        driver.switchTo().window(handle);
    }

    /**
     * Closes the current window and switches to another.
     *
     * @param handleToReturnTo window to activate afterwards
     */
    protected void closeCurrentWindowAndSwitchTo(String handleToReturnTo) {
        log.info(() -> "Close window " + driver.getWindowHandle());
        driver.close();
        switchToWindow(handleToReturnTo);
    }

    // ---------------------------------------------------------------- alerts

    /** @return text of the open alert (waits for it) */
    protected String getAlertText() {
        return wait.alertPresent().getText();
    }

    /** Accepts the open alert (waits for it). */
    protected void acceptAlert() {
        Alert alert = wait.alertPresent();
        log.info(() -> "Accept alert: " + alert.getText());
        alert.accept();
    }

    /** Dismisses the open alert (waits for it). */
    protected void dismissAlert() {
        Alert alert = wait.alertPresent();
        log.info(() -> "Dismiss alert: " + alert.getText());
        alert.dismiss();
    }

    /** @return true if an alert is currently open (no wait) */
    public boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }
}
