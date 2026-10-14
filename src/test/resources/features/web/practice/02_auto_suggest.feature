@ui @autoSuggest
Feature: Module 2 - Country auto-suggest
  As a user entering a country
  I want matching suggestions while I type
  So that I can pick a country quickly and accurately

  Background:
    Given user is on the automation practice page

  @smoke
  Scenario Outline: Typing "<partial>" suggests and selects <country>
    When user types "<partial>" in the country field
    Then the suggestions should include "<country>"
    When user selects "<country>" from the suggestions
    Then the country field should contain "<country>"

    Examples:
      | partial                 | country                     |
      | autosuggest.partial.ind | autosuggest.country.india   |
      | autosuggest.partial.uni | autosuggest.country.uk      |
      | autosuggest.partial.aus | autosuggest.country.austria |

  @regression
  Scenario: Every suggestion matches the typed text
    When user types "autosuggest.partial.uni" in the country field
    Then every suggestion should contain "autosuggest.partial.uni"
    And the suggestions should include "autosuggest.country.usa"

  @regression
  Scenario: A suggestion can be chosen with the keyboard
    When user types "autosuggest.partial.ind" in the country field
    And user moves through the suggestions with the keyboard to "autosuggest.country.indonesia"
    Then the country field should contain "autosuggest.country.indonesia"

  @negative
  Scenario: A single character does not trigger suggestions
    When user types "autosuggest.single.char" in the country field
    Then no country suggestions should be displayed

  @negative
  Scenario: Text that matches no country shows no suggestions
    When user types "autosuggest.unknown.text" in the country field
    Then no country suggestions should be displayed
