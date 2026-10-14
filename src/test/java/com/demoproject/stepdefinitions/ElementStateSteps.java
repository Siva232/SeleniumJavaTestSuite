package com.demoproject.stepdefinitions;

import com.demoproject.pages.ElementStatePage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;

/**
 * Module 5 (element state) - hide/show textbox and mouse hover menu.
 */
public class ElementStateSteps extends BaseSteps {

    private ElementStatePage page;

    private ElementStatePage page() {
        if (page == null) {
            page = new ElementStatePage();
        }
        return page;
    }

    @When("user hides the textbox")
    public void userHidesTheTextbox() {
        page().hideTextbox();
    }

    @When("user shows the textbox")
    public void userShowsTheTextbox() {
        page().showTextbox();
    }

    @Then("the textbox should be displayed")
    public void theTextboxShouldBeDisplayed() {
        Assert.assertTrue(page().isTextboxDisplayed(), "Textbox is hidden");
    }

    @Then("the textbox should be hidden")
    public void theTextboxShouldBeHidden() {
        Assert.assertFalse(page().isTextboxDisplayed(), "Textbox is displayed");
    }

    @When("user types {string} into the textbox")
    public void userTypesIntoTheTextbox(String textKey) {
        page().typeIntoTextbox(data(textKey));
    }

    @Then("the textbox should contain {string}")
    public void theTextboxShouldContain(String textKey) {
        Assert.assertEquals(page().getTextboxValue(), data(textKey), "Textbox value");
    }

    @When("user hovers over the mouse hover button")
    public void userHoversOverTheMouseHoverButton() {
        page().hoverOverMenu();
    }

    @Then("the hover menu should show the options")
    public void theHoverMenuShouldShowTheOptions(List<String> optionKeys) {
        Assert.assertEquals(page().getHoverOptions(), dataList(optionKeys), "Hover menu options");
    }

    @When("user selects the hover option {string}")
    public void userSelectsTheHoverOption(String optionKey) {
        page().selectHoverOption(data(optionKey));
    }

    @Then("the page URL should end with {string}")
    public void thePageUrlShouldEndWith(String suffixKey) {
        Assert.assertTrue(page().urlEndsWith(data(suffixKey)), "URL does not end with '" + data(suffixKey) + "'");
    }
}
