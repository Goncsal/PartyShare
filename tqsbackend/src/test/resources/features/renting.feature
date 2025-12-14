@rental
Feature: Item Rental Journey

  Scenario: User rents an item successfully
    Given the following items exist:
      | name      | price | category |
      | Party Hat | 50.0  | Party    |
    And a user exists with email "renter@email.com"
    And I am logged in as a renter with email "renter@email.com" and password "password"
    And I am on the search page
    When I select the first item to view details
    And I choose to rent the item
    And I fill in the rental dates from "2025-12-31" to "2026-01-06"
    And I make an offer of "50" per day
    And I submit the rental request
    Then I should see the confirmation message "Booking request sent! Waiting"
