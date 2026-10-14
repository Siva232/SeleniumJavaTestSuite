package com.demoproject.pages;

import org.openqa.selenium.By;

/**
 * "Switch To Alert" section: name field plus Alert and Confirm buttons.
 */
public class AlertsPage extends PracticePage {

    private static final By NAME_INPUT = By.id("name");
    private static final By ALERT_BUTTON = By.id("alertbtn");
    private static final By CONFIRM_BUTTON = By.id("confirmbtn");

    /** @param name name shown in the popup */
    public void enterName(String name) {
        scrollIntoView(NAME_INPUT);
        type(NAME_INPUT, name);
    }

    /** Opens the alert popup. */
    public void clickAlertButton() {
        click(ALERT_BUTTON);
    }

    /** Opens the confirm popup. */
    public void clickConfirmButton() {
        click(CONFIRM_BUTTON);
    }

    /** @return text of the open popup */
    public String getPopupText() {
        return getAlertText();
    }

    /** Clicks OK on the popup. */
    public void acceptPopup() {
        acceptAlert();
    }

    /** Clicks Cancel on the popup. */
    public void dismissPopup() {
        dismissAlert();
    }

    /** @return current value of the name field */
    public String getNameFieldValue() {
        return getValue(NAME_INPUT);
    }
}
