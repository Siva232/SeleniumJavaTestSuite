package com.demoproject.stepdefinitions;

import com.demoproject.pages.PracticePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.testng.Assert;

/**
 * Steps shared by all practice-page modules.
 */
public class CommonSteps extends BaseSteps {

    @Given("user is on the automation practice page")
    public void userIsOnThePracticePage() {
        new PracticePage().open();
    }

    @Then("the practice page heading should be {string}")
    public void thePracticePageHeadingShouldBe(String headingKey) {
        Assert.assertEquals(new PracticePage().getHeading(), data(headingKey), "Practice page heading");
    }
}
