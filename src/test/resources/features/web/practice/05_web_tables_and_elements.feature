@ui @webTables
Feature: Module 5 - Web tables and element state
  As a user reading the practice page
  I want the tables to contain consistent data and dynamic elements to behave
  So that I can trust what the page displays

  Background:
    Given user is on the automation practice page

  @smoke
  Scenario: Course table has the expected structure
    When user views the course table
    Then the course table should have "webtables.course.row.count" rows
    And the course table headers should be
      | webtables.header.instructor |
      | webtables.header.course     |
      | webtables.header.price      |

  @regression
  Scenario Outline: Course price lookup for <course>
    When user views the course table
    Then the price of course "<course>" should be "<price>"

    Examples:
      | course                     | price                            |
      | webtables.course.selenium  | webtables.course.selenium.price  |
      | webtables.course.soapui    | webtables.course.soapui.price    |
      | webtables.course.resume    | webtables.course.resume.price    |

  @regression
  Scenario: Course prices add up to the expected total
    Then the total of all course prices should be "webtables.course.price.total"

  @smoke
  Scenario: Fixed-header table total matches the sum of its Amount column
    Then the displayed total amount should equal the sum of the amount column

  @regression
  Scenario: Employees can be filtered by city
    Then the employees located in "webtables.city.chennai" should be
      | webtables.employee.alex    |
      | webtables.employee.ivory   |
      | webtables.employee.joe     |
      | webtables.employee.ronaldo |
    And the employees located in "webtables.city.pune" should be
      | webtables.employee.jack |

  @regression @elementState
  Scenario: Textbox can be hidden and shown again
    Then the textbox should be displayed
    When user hides the textbox
    Then the textbox should be hidden
    When user shows the textbox
    And user types "elements.textbox.text" into the textbox
    Then the textbox should be displayed

  @smoke @elementState
  Scenario: Mouse hover reveals the menu and Top scrolls to the top
    When user hovers over the mouse hover button
    Then the hover menu should show the options
      | elements.hover.option.top    |
      | elements.hover.option.reload |
    When user selects the hover option "elements.hover.option.top"
    Then the page URL should end with "elements.top.url.suffix"
