package com.demoproject.stepdefinitions;

import com.demoproject.pages.FormControlsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;

/**
 * Module 1 - radio buttons, checkboxes and dropdown.
 */
public class FormControlsSteps extends BaseSteps {

    private FormControlsPage page;

    private FormControlsPage page() {
        if (page == null) {
            page = new FormControlsPage();
        }
        return page;
    }

    @When("user selects the radio button {string}")
    public void userSelectsTheRadioButton(String radioKey) {
        page().selectRadio(data(radioKey));
    }

    @Then("the radio button {string} should be selected")
    public void theRadioButtonShouldBeSelected(String radioKey) {
        Assert.assertTrue(page().isRadioSelected(data(radioKey)), "Radio '" + data(radioKey) + "' is not selected");
    }

    @Then("the radio button {string} should not be selected")
    public void theRadioButtonShouldNotBeSelected(String radioKey) {
        Assert.assertFalse(page().isRadioSelected(data(radioKey)), "Radio '" + data(radioKey) + "' is selected");
    }

    @Then("exactly one radio button should be selected")
    public void exactlyOneRadioButtonShouldBeSelected() {
        Assert.assertEquals(page().selectedRadioCount(), 1L, "Number of selected radio buttons");
    }

    @When("user selects the checkbox {string}")
    public void userSelectsTheCheckbox(String checkboxKey) {
        page().checkCheckbox(data(checkboxKey));
    }

    @When("user unselects the checkbox {string}")
    public void userUnselectsTheCheckbox(String checkboxKey) {
        page().uncheckCheckbox(data(checkboxKey));
    }

    @When("user selects all checkboxes")
    public void userSelectsAllCheckboxes() {
        page().checkAllCheckboxes();
    }

    @Then("the checkbox {string} should be selected")
    public void theCheckboxShouldBeSelected(String checkboxKey) {
        Assert.assertTrue(page().isCheckboxSelected(data(checkboxKey)), "Checkbox '" + data(checkboxKey) + "' not selected");
    }

    @Then("the checkbox {string} should not be selected")
    public void theCheckboxShouldNotBeSelected(String checkboxKey) {
        Assert.assertFalse(page().isCheckboxSelected(data(checkboxKey)), "Checkbox '" + data(checkboxKey) + "' selected");
    }

    @Then("{string} checkboxes should be selected")
    public void checkboxesShouldBeSelected(String countKey) {
        Assert.assertEquals(page().selectedCheckboxCount(), (long) dataInt(countKey), "Number of selected checkboxes");
    }

    @Then("the page should show {string} checkboxes")
    public void thePageShouldShowCheckboxes(String countKey) {
        Assert.assertEquals(page().checkboxCount(), dataInt(countKey), "Number of checkboxes");
    }

    @When("user selects the dropdown option {string}")
    public void userSelectsTheDropdownOption(String optionKey) {
        page().selectDropdownOption(data(optionKey));
    }

    @Then("the selected dropdown option should be {string}")
    public void theSelectedDropdownOptionShouldBe(String optionKey) {
        Assert.assertEquals(page().getSelectedDropdownOption(), data(optionKey), "Selected dropdown option");
    }

    @Then("the dropdown should contain the options")
    public void theDropdownShouldContainTheOptions(List<String> optionKeys) {
        Assert.assertEquals(page().getDropdownOptions(), dataList(optionKeys), "Dropdown options");
    }
}
