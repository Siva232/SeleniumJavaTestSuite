@ui @windows
Feature: Module 4 - Windows, tabs and iframes
  As a user of the practice page
  I want links that open new windows or tabs and embedded content in an iframe
  So that I can move between browsing contexts without losing the practice page

  Background:
    Given user is on the automation practice page

  @smoke
  Scenario: Open Window opens a separate browser window
    When user opens a new window from the practice page
    Then the active window URL should contain "windows.child.url.fragment"
    And the number of open windows should be "windows.count.two"

  @regression
  Scenario: Closing the new window returns to the practice page
    When user opens a new window from the practice page
    And user closes the active window and returns to the practice page
    Then the number of open windows should be "windows.count.one"
    And the practice page heading should be "practice.page.heading"

  @smoke
  Scenario: Open Tab opens the link in a new tab
    When user opens a new tab from the practice page
    Then the active window URL should contain "windows.child.url.fragment"
    And the number of open windows should be "windows.count.two"
    When user closes the active window and returns to the practice page
    Then the practice page heading should be "practice.page.heading"

  @smoke
  Scenario: Content inside the courses iframe is accessible
    When user switches to the courses iframe
    Then the iframe should contain a link "frames.link.mentorship"
    And the iframe should contain at least "frames.min.link.count" links

  @regression
  Scenario: Leaving the iframe restores access to the main page
    When user switches to the courses iframe
    And user switches back to the main page
    Then the practice page heading should be "practice.page.heading"
