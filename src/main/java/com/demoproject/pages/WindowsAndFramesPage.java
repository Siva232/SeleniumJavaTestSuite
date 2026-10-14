package com.demoproject.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "Switch Window", "Switch Tab" and "iFrame" sections of the practice page.
 */
public class WindowsAndFramesPage extends PracticePage {

    private static final By OPEN_WINDOW = By.id("openwindow");
    private static final By OPEN_TAB = By.id("opentab");
    private static final By COURSES_FRAME = By.id("courses-iframe");
    private static final By FRAME_LINKS = By.cssSelector("a[href]");

    /**
     * Opens the popup window and switches to it.
     *
     * @return handle of the practice page window, to return to later
     */
    public String openNewWindow() {
        return openAndSwitch(OPEN_WINDOW);
    }

    /**
     * Opens the link in a new tab and switches to it.
     *
     * @return handle of the practice page window, to return to later
     */
    public String openNewTab() {
        return openAndSwitch(OPEN_TAB);
    }

    /**
     * @param fragment expected URL substring of the active window
     * @return true once the active window's URL contains it
     */
    public boolean activeWindowUrlContains(String fragment) {
        return wait.urlContains(fragment);
    }

    /**
     * @param parentHandle window to return to
     */
    public void closeActiveWindowAndReturn(String parentHandle) {
        closeCurrentWindowAndSwitchTo(parentHandle);
    }

    /** Switches into the courses iframe. */
    public void switchToCoursesFrame() {
        scrollIntoView(COURSES_FRAME);
        switchToFrame(COURSES_FRAME);
    }

    /** Leaves the iframe. */
    public void switchToMainPage() {
        switchToDefaultContent();
    }

    /**
     * Uses {@code textContent} so links collapsed into a responsive menu are still found.
     *
     * @param linkText expected link text (case-insensitive, substring)
     * @return true if a link with that text exists in the current frame
     */
    public boolean frameHasLink(String linkText) {
        wait.present(FRAME_LINKS);
        String expected = linkText.toLowerCase();
        return findAll(FRAME_LINKS).stream()
                .map(e -> e.getDomProperty("textContent"))
                .anyMatch(text -> text != null && text.trim().toLowerCase().contains(expected));
    }

    /** @return number of links in the current frame */
    public int frameLinkCount() {
        wait.present(FRAME_LINKS);
        List<WebElement> links = findAll(FRAME_LINKS);
        return links.size();
    }

    private String openAndSwitch(By trigger) {
        String parent = currentWindow();
        Set<String> before = new HashSet<>(driver.getWindowHandles());
        scrollIntoView(trigger);
        click(trigger);
        switchToNewWindow(before);
        return parent;
    }
}
