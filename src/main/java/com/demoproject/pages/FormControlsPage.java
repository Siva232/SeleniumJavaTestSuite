package com.demoproject.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Radio buttons, checkboxes and the static dropdown of the practice page.
 */
public class FormControlsPage extends PracticePage {

    private static final By RADIO_BUTTONS = By.cssSelector("#radio-btn-example input.radioButton");
    private static final String RADIO_BY_VALUE = "#radio-btn-example input.radioButton[value='%s']";
    private static final By CHECKBOXES = By.cssSelector("#checkbox-example input[type='checkbox']");
    private static final String CHECKBOX_BY_VALUE = "#checkbox-example input[value='%s']";
    private static final By DROPDOWN = By.id("dropdown-class-example");

    /** @param value radio value, e.g. radio2 */
    public void selectRadio(String value) {
        By radio = radio(value);
        scrollIntoView(radio);
        click(radio);
    }

    /**
     * @param value radio value
     * @return whether it is selected
     */
    public boolean isRadioSelected(String value) {
        return isSelected(radio(value));
    }

    /** @return number of selected radio buttons */
    public long selectedRadioCount() {
        return findAll(RADIO_BUTTONS).stream().filter(WebElement::isSelected).count();
    }

    /** @param value checkbox value; selects it if not already selected */
    public void checkCheckbox(String value) {
        if (!isCheckboxSelected(value)) {
            click(checkbox(value));
        }
    }

    /** @param value checkbox value; clears it if selected */
    public void uncheckCheckbox(String value) {
        if (isCheckboxSelected(value)) {
            click(checkbox(value));
        }
    }

    /** Selects every checkbox. */
    public void checkAllCheckboxes() {
        for (WebElement box : wait.allVisible(CHECKBOXES)) {
            if (!box.isSelected()) {
                box.click();
            }
        }
    }

    /**
     * @param value checkbox value
     * @return selection state
     */
    public boolean isCheckboxSelected(String value) {
        return isSelected(checkbox(value));
    }

    /** @return number of selected checkboxes */
    public long selectedCheckboxCount() {
        return findAll(CHECKBOXES).stream().filter(WebElement::isSelected).count();
    }

    /** @return total number of checkboxes */
    public int checkboxCount() {
        return wait.allVisible(CHECKBOXES).size();
    }

    /** @param optionText visible option text */
    public void selectDropdownOption(String optionText) {
        selectByVisibleText(DROPDOWN, optionText);
    }

    /** @return selected option text */
    public String getSelectedDropdownOption() {
        return getSelectedOption(DROPDOWN);
    }

    /** @return all option texts, in page order */
    public List<String> getDropdownOptions() {
        return getOptions(DROPDOWN);
    }

    private static By radio(String value) {
        return By.cssSelector(String.format(RADIO_BY_VALUE, value));
    }

    private static By checkbox(String value) {
        return By.cssSelector(String.format(CHECKBOX_BY_VALUE, value));
    }
}
