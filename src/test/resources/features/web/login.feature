@ui @login
Feature: Login - Rahul Shetty Academy login practice page
  As a registered user
  I want to sign in with my credentials
  So that I reach the shop, and invalid attempts are rejected with a clear message

  Background:
    Given user is on the login page

  @smoke @positive
  Scenario Outline: Successful login with <userType> credentials
    When user logs in with "<username>" and "<password>"
    Then user should be redirected to "<landingPage>"

    Examples:
      | userType | username             | password             | landingPage                |
      | valid    | login.valid.username | login.valid.password | login.success.url.fragment |

  @negative
  Scenario Outline: Login is rejected for <case>
    When user logs in with "<username>" and "<password>"
    Then login error message should contain "<errorMessage>"

    Examples:
      | case              | username               | password               | errorMessage             |
      | invalid password  | login.valid.username   | login.invalid.password | login.error.incorrect    |
      | invalid username  | login.invalid.username | login.valid.password   | login.error.incorrect    |
      | expired password  | login.valid.username   | login.old.password     | login.error.old.password |
      | empty credentials | login.empty.username   | login.empty.password   | login.error.empty        |

  @smoke
  Scenario: Configured application user can log in
    When user logs in with the configured application credentials
    Then user should be redirected to "login.success.url.fragment"
