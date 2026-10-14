package com.demoproject.stepdefinitions;

import com.demoproject.context.ScenarioContext;
import com.demoproject.pages.AlertsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Module 3 - JavaScript alert and confirm popups.
 */
public class AlertsSteps extends BaseSteps {

    private static final String NAME_TOKEN = "{name}";

    private AlertsPage page;

    private AlertsPage page() {
        if (page == null) {
            page = new AlertsPage();
        }
        return page;
    }

    @When("user enters {string} in the alert name field")
    public void userEntersInTheAlertNameField(String nameKey) {
        String name = data(nameKey);
        ScenarioContext.put(ScenarioContext.Key.ALERT_NAME, name);
        page().enterName(name);
    }

    @When("user clicks the alert button")
    public void userClicksTheAlertButton() {
        page().clickAlertButton();
    }

    @When("user clicks the confirm button")
    public void userClicksTheConfirmButton() {
        page().clickConfirmButton();
    }

    @Then("the popup text should match {string} for the entered name")
    public void thePopupTextShouldMatchForTheEnteredName(String templateKey) {
        String name = ScenarioContext.get(ScenarioContext.Key.ALERT_NAME, String.class);
        String expected = data(templateKey).replace(NAME_TOKEN, name);
        Assert.assertEquals(page().getPopupText(), expected, "Popup text");
    }

    @When("user accepts the popup")
    public void userAcceptsThePopup() {
        page().acceptPopup();
    }

    @When("user dismisses the popup")
    public void userDismissesThePopup() {
        page().dismissPopup();
    }

    @Then("no popup should be open")
    public void noPopupShouldBeOpen() {
        Assert.assertFalse(page().isAlertPresent(), "A popup is still open");
    }

    @Then("the alert name field should be empty")
    public void theAlertNameFieldShouldBeEmpty() {
        Assert.assertEquals(page().getNameFieldValue(), "", "Name field value after the popup");
    }
}
