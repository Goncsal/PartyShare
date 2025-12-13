Feature: Item Management
  As a user (owner)
  I want to manage my items
  So that I can rent them out

  Scenario: Create a new item
    Given I am a registered user with email "owner@email.com"
    When I create an item with name "Party Speaker", price 50.0, and category "ELECTRONICS"
    Then the item should be created successfully
    And the item should be available in the system

  Scenario: Search for items by keyword
    Given the following items exist:
      | name           | price | category    |
      | Party Speaker  | 50.0  | ELECTRONICS |
      | Disco Ball     | 20.0  | DECORATION  |
      | Table Set      | 30.0  | FURNITURE   |
    When I search for items with keyword "Party"
    Then I should find 1 item
    And the item name should be "Party Speaker"
