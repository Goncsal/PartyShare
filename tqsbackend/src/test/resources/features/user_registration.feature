Feature: User Registration
  As a new user
  I want to register an account
  So that I can use the application

  Scenario: Successful registration
    Given I have valid registration details
      | name     | email              | password | role   |
      | John Doe | john.doe@email.com | password | RENTER |
    When I register with these details
    Then the registration should be successful
    And the user should be in the system

  Scenario: Registration with existing email
    Given a user exists with email "jane.doe@email.com"
    When I register with name "Jane Doe", email "jane.doe@email.com", password "password", and role "RENTER"
    Then the registration should fail with error "Failed to register user: Email already exists."
