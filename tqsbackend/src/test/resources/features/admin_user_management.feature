@admin_user_management
Feature: Admin User Management Journey

  Scenario: Admin deactivates a user
    Given I am logged in as an admin
    And a user exists with name "Target User", email "target@email.com", and role "RENTER"
    And I am on the admin dashboard page
    When I navigate to User Management
    And I search for "Target User"
    And I view the profile of "Target User"
    And I click "Deactivate User"
    Then the user "Target User" should be "Inactive"
