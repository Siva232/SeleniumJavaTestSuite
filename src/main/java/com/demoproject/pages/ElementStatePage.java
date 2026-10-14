package com.demoproject.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

/**
 * "Element Displayed" (hide/show) and "Mouse Hover" sections of the practice page.
 */
public class ElementStatePage extends PracticePage {

    private static final By HIDE_BUTTON = By.id("hide-textbox");
    private static final By SHOW_BUTTON = By.id("show-textbox");
    private static final By TOGGLED_TEXTBOX = By.id("displayed-text");
    private static final By HOVER_BUTTON = By.id("mousehover");
    private static final By HOVER_OPTIONS = By.cssSelector(".mouse-hover-content a");
    private static final String HOVER_OPTION_BY_TEXT = "//div[@class='mouse-hover-content']/a[normalize-space()='%s']";

    /** Hides the textbox and waits until it is hidden. */
    public void hideTextbox() {
        scrollIntoView(HIDE_BUTTON);
        click(HIDE_BUTTON);
        wait.invisible(TOGGLED_TEXTBOX);
    }

    /** Shows the textbox and waits until it is visible. */
    public void showTextbox() {
        scrollIntoView(SHOW_BUTTON);
        click(SHOW_BUTTON);
        wait.visible(TOGGLED_TEXTBOX);
    }

    /** @return whether the toggled textbox is displayed */
    public boolean isTextboxDisplayed() {
        return isDisplayed(TOGGLED_TEXTBOX);
    }

    /** @param text text to type into the toggled textbox */
    public void typeIntoTextbox(String text) {
        type(TOGGLED_TEXTBOX, text);
    }

    /** @return the toggled textbox's value */
    public String getTextboxValue() {
        return getValue(TOGGLED_TEXTBOX);
    }

    /** Moves the mouse over the hover button, revealing its menu. */
    public void hoverOverMenu() {
        hover(HOVER_BUTTON);
    }

    /** @return option texts of the revealed hover menu */
    public List<String> getHoverOptions() {
        return getTexts(HOVER_OPTIONS);
    }

    /**
     * Hovers and clicks a menu option in one continuous mouse movement so the menu stays open.
     *
     * @param optionText option text, e.g. Top
     */
    public void selectHoverOption(String optionText) {
        hoverOverMenu();
        By option = By.xpath(String.format(HOVER_OPTION_BY_TEXT, optionText));
        log.info(() -> "Click hover option '" + optionText + "'");
        new Actions(driver).moveToElement(wait.visible(option)).click().perform();
    }

    /**
     * @param suffix expected URL ending, e.g. #top
     * @return true once the URL ends with it
     */
    public boolean urlEndsWith(String suffix) {
        return wait.until(d -> d.getCurrentUrl().endsWith(suffix), "URL ending with '" + suffix + "'");
    }
}
