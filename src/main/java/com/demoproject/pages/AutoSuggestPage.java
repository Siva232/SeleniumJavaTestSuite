package com.demoproject.pages;

import com.demoproject.exceptions.FrameworkException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

/**
 * Country auto-suggest (jQuery UI autocomplete) of the practice page.
 */
public class AutoSuggestPage extends PracticePage {

    private static final By COUNTRY_INPUT = By.id("autocomplete");
    private static final By SUGGESTIONS = By.cssSelector("ul.ui-autocomplete li.ui-menu-item div");
    private static final Duration NO_SUGGESTION_WINDOW = Duration.ofSeconds(2);

    /** @param text partial country name */
    public void typeCountry(String text) {
        scrollIntoView(COUNTRY_INPUT);
        type(COUNTRY_INPUT, text);
    }

    /** @return suggestion texts (waits for the list to appear) */
    public List<String> getSuggestions() {
        return getTexts(SUGGESTIONS);
    }

    /**
     * @param country exact suggestion text to click
     */
    public void selectSuggestion(String country) {
        WebElement match = wait.allVisible(SUGGESTIONS).stream()
                .filter(e -> e.getText().trim().equals(country))
                .findFirst()
                .orElseThrow(() -> new FrameworkException("Suggestion '" + country + "' not shown. Shown: "
                        + getSuggestions()));
        log.info(() -> "Click suggestion '" + country + "'");
        match.click();
    }

    /**
     * Moves through the open suggestion list with the down arrow until the field shows
     * {@code country}, then confirms with Enter.
     *
     * @param country target suggestion
     */
    public void selectSuggestionWithKeyboard(String country) {
        int count = wait.allVisible(SUGGESTIONS).size();
        for (int i = 0; i < count; i++) {
            sendKeys(COUNTRY_INPUT, Keys.ARROW_DOWN);
            if (country.equals(getCountryValue())) {
                sendKeys(COUNTRY_INPUT, Keys.ENTER);
                return;
            }
        }
        throw new FrameworkException("'" + country + "' was not reachable with the keyboard among " + count
                + " suggestions");
    }

    /** @return current value of the country field */
    public String getCountryValue() {
        return getValue(COUNTRY_INPUT);
    }

    /** @return true if no suggestion list appears within a short observation window */
    public boolean noSuggestionsAppear() {
        return wait.staysInvisible(SUGGESTIONS, NO_SUGGESTION_WINDOW);
    }
}
