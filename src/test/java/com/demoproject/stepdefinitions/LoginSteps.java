package com.demoproject.stepdefinitions;

import com.demoproject.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Steps for the login practice page.
 */
public class LoginSteps extends BaseSteps {

    private LoginPage loginPage;

    @Given("user is on the login page")
    public void userIsOnTheLoginPage() {
        loginPage = new LoginPage().open();
    }

    @When("user logs in with {string} and {string}")
    public void userLogsInWith(String usernameKey, String passwordKey) {
        loginPage.login(data(usernameKey), data(passwordKey));
    }

    @When("user logs in with the configured application credentials")
    public void userLogsInWithConfiguredCredentials() {
        loginPage.loginWithConfiguredUser();
    }

    @Then("user should be redirected to {string}")
    public void userShouldBeRedirectedTo(String urlFragmentKey) {
        String fragment = data(urlFragmentKey);
        Assert.assertTrue(loginPage.isRedirectedTo(fragment), "Expected landing URL to contain '" + fragment + "'");
    }

    @Then("login error message should contain {string}")
    public void loginErrorMessageShouldContain(String messageKey) {
        String expected = data(messageKey);
        String actual = loginPage.getErrorMessage();
        Assert.assertTrue(actual.contains(expected),
                "Login error was '" + actual + "', expected it to contain '" + expected + "'");
    }
}
