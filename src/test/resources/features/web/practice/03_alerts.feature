@ui @alerts
Feature: Module 3 - JavaScript alert and confirm popups
  As a user of the practice page
  I want personalised alert and confirm popups
  So that I can acknowledge or cancel an action

  Background:
    Given user is on the automation practice page

  @smoke
  Scenario Outline: Alert popup greets <name>
    When user enters "<name>" in the alert name field
    And user clicks the alert button
    Then the popup text should match "alerts.alert.template" for the entered name
    When user accepts the popup
    Then no popup should be open

    Examples:
      | name                 |
      | alerts.name.primary  |
      | alerts.name.alex     |
      | alerts.name.dynamic  |

  @regression
  Scenario: Confirm popup can be accepted
    When user enters "alerts.name.jordan" in the alert name field
    And user clicks the confirm button
    Then the popup text should match "alerts.confirm.template" for the entered name
    When user accepts the popup
    Then no popup should be open

  @regression
  Scenario: Confirm popup can be dismissed
    When user enters "alerts.name.alex" in the alert name field
    And user clicks the confirm button
    Then the popup text should match "alerts.confirm.template" for the entered name
    When user dismisses the popup
    Then no popup should be open

  @regression
  Scenario: Name field is cleared after the alert is shown
    When user enters "alerts.name.primary" in the alert name field
    And user clicks the alert button
    And user accepts the popup
    Then the alert name field should be empty
