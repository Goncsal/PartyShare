@renter_search
Feature: UI Search Functionality

  Scenario: Search and filter items
    Given the following items exist:
      | name      | price | category |
      | Party Hat | 50.0  | Party    |
    And I am on the home page
    When I search for "party" in the UI
    And I filter by max price "60"
    And I click on the item details
    Then I should be on the item details page
