@ui @formControls
Feature: Module 1 - Form controls (radio buttons, checkboxes, dropdown)
  As a user of the automation practice page
  I want the basic form controls to respond correctly
  So that selections I make are reflected on the page

  Background:
    Given user is on the automation practice page

  @smoke
  Scenario Outline: Selecting radio button <radio> marks it as selected
    When user selects the radio button "<radio>"
    Then the radio button "<radio>" should be selected
    And exactly one radio button should be selected

    Examples:
      | radio                    |
      | formcontrols.radio.first  |
      | formcontrols.radio.second |
      | formcontrols.radio.third  |

  @regression
  Scenario: Selecting a second radio button clears the first one
    When user selects the radio button "formcontrols.radio.first"
    And user selects the radio button "formcontrols.radio.third"
    Then the radio button "formcontrols.radio.third" should be selected
    And the radio button "formcontrols.radio.first" should not be selected
    And exactly one radio button should be selected

  @regression
  Scenario: Checkboxes can be selected and unselected independently
    Then the page should show "formcontrols.checkbox.total" checkboxes
    And "formcontrols.checkbox.none" checkboxes should be selected
    When user selects the checkbox "formcontrols.checkbox.first"
    And user selects the checkbox "formcontrols.checkbox.third"
    Then "formcontrols.checkbox.two" checkboxes should be selected
    When user unselects the checkbox "formcontrols.checkbox.first"
    Then the checkbox "formcontrols.checkbox.first" should not be selected
    And the checkbox "formcontrols.checkbox.third" should be selected

  @smoke
  Scenario: All checkboxes can be selected at once
    When user selects all checkboxes
    Then "formcontrols.checkbox.total" checkboxes should be selected

  @regression
  Scenario: Dropdown lists the default and all options
    Then the selected dropdown option should be "formcontrols.dropdown.default"
    And the dropdown should contain the options
      | formcontrols.dropdown.default |
      | formcontrols.dropdown.option1 |
      | formcontrols.dropdown.option2 |
      | formcontrols.dropdown.option3 |

  @smoke
  Scenario Outline: Selecting dropdown option <option>
    When user selects the dropdown option "<option>"
    Then the selected dropdown option should be "<option>"

    Examples:
      | option                        |
      | formcontrols.dropdown.option1 |
      | formcontrols.dropdown.option3 |
