package com.demoproject.stepdefinitions;

import com.demoproject.pages.AutoSuggestPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;
import java.util.Locale;

/**
 * Module 2 - country auto-suggest.
 */
public class AutoSuggestSteps extends BaseSteps {

    private AutoSuggestPage page;

    private AutoSuggestPage page() {
        if (page == null) {
            page = new AutoSuggestPage();
        }
        return page;
    }

    @When("user types {string} in the country field")
    public void userTypesInTheCountryField(String textKey) {
        page().typeCountry(data(textKey));
    }

    @Then("the suggestions should include {string}")
    public void theSuggestionsShouldInclude(String countryKey) {
        List<String> suggestions = page().getSuggestions();
        Assert.assertTrue(suggestions.contains(data(countryKey)),
                "Suggestions " + suggestions + " do not include '" + data(countryKey) + "'");
    }

    @Then("every suggestion should contain {string}")
    public void everySuggestionShouldContain(String textKey) {
        String expected = data(textKey).toLowerCase(Locale.ROOT);
        List<String> suggestions = page().getSuggestions();
        Assert.assertFalse(suggestions.isEmpty(), "No suggestions were shown");
        suggestions.forEach(s -> Assert.assertTrue(s.toLowerCase(Locale.ROOT).contains(expected),
                "Suggestion '" + s + "' does not contain '" + expected + "'"));
    }

    @When("user selects {string} from the suggestions")
    public void userSelectsFromTheSuggestions(String countryKey) {
        page().selectSuggestion(data(countryKey));
    }

    @When("user moves through the suggestions with the keyboard to {string}")
    public void userMovesThroughTheSuggestionsWithTheKeyboardTo(String countryKey) {
        page().selectSuggestionWithKeyboard(data(countryKey));
    }

    @Then("the country field should contain {string}")
    public void theCountryFieldShouldContain(String countryKey) {
        Assert.assertEquals(page().getCountryValue(), data(countryKey), "Country field value");
    }

    @Then("no country suggestions should be displayed")
    public void noCountrySuggestionsShouldBeDisplayed() {
        Assert.assertTrue(page().noSuggestionsAppear(), "Country suggestions were displayed");
    }
}
