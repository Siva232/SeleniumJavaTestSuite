package com.demoproject.stepdefinitions;

import com.demoproject.context.ScenarioContext;
import com.demoproject.pages.WindowsAndFramesPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Module 4 - new windows, new tabs and iframes.
 */
public class WindowsAndFramesSteps extends BaseSteps {

    private WindowsAndFramesPage page;

    private WindowsAndFramesPage page() {
        if (page == null) {
            page = new WindowsAndFramesPage();
        }
        return page;
    }

    @When("user opens a new window from the practice page")
    public void userOpensANewWindow() {
        ScenarioContext.put(ScenarioContext.Key.PARENT_WINDOW, page().openNewWindow());
    }

    @When("user opens a new tab from the practice page")
    public void userOpensANewTab() {
        ScenarioContext.put(ScenarioContext.Key.PARENT_WINDOW, page().openNewTab());
    }

    @Then("the active window URL should contain {string}")
    public void theActiveWindowUrlShouldContain(String fragmentKey) {
        String fragment = data(fragmentKey);
        Assert.assertTrue(page().activeWindowUrlContains(fragment), "Active window URL does not contain '" + fragment + "'");
    }

    @Then("the number of open windows should be {string}")
    public void theNumberOfOpenWindowsShouldBe(String countKey) {
        Assert.assertEquals(page().getWindowCount(), dataInt(countKey), "Open windows");
    }

    @When("user closes the active window and returns to the practice page")
    public void userClosesTheActiveWindowAndReturns() {
        page().closeActiveWindowAndReturn(ScenarioContext.get(ScenarioContext.Key.PARENT_WINDOW, String.class));
    }

    @When("user switches to the courses iframe")
    public void userSwitchesToTheCoursesIframe() {
        page().switchToCoursesFrame();
    }

    @When("user switches back to the main page")
    public void userSwitchesBackToTheMainPage() {
        page().switchToMainPage();
    }

    @Then("the iframe should contain a link {string}")
    public void theIframeShouldContainALink(String linkKey) {
        Assert.assertTrue(page().frameHasLink(data(linkKey)), "No '" + data(linkKey) + "' link inside the iframe");
    }

    @Then("the iframe should contain at least {string} links")
    public void theIframeShouldContainAtLeastLinks(String countKey) {
        int count = page().frameLinkCount();
        Assert.assertTrue(count >= dataInt(countKey), "Iframe has only " + count + " links");
    }
}
