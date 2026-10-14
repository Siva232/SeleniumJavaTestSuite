package com.demoproject.pages;

import org.openqa.selenium.By;

/**
 * Common parent for the sections of the Automation Practice page ({@code app.url}).
 */
public class PracticePage extends BasePage {

    private static final By HEADING = By.tagName("h1");

    /**
     * Opens the practice page.
     *
     * @return this page
     */
    public PracticePage open() {
        navigateTo(config.getAppUrl());
        return this;
    }

    /** @return the main page heading */
    public String getHeading() {
        return getText(HEADING);
    }
}
