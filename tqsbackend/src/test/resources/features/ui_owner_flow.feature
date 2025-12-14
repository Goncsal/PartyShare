@owner_flow
Feature: Owner UI Flow

  Scenario: Register and Login as Owner
    Given I am on the home page
    When I navigate to the registration page
    And I register in the UI with name "TestOwner", email "owner_ui_test@partyshare.com", password "admin123", and role "OWNER"
    Then I should be redirected to the login page
    When I login with email "owner_ui_test@partyshare.com" and password "admin123"
    Then I should be on the home page
