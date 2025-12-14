@create_item
Feature: Item Creation Journey

  Scenario: Owner creates a new item successfully
    Given the following categories exist:
      | Party |
    Given an owner exists with email "owner@email.com"
    And I am logged in as an owner with email "owner@email.com" and password "password"
    And I am on the dashboard page
    When I click on "Add New Item"
    And I fill the item form with:
      | name        | JBL SPEAKER                                      |
      | description | This is a JBL speaker                            |
      | price       | 20                                               |
      | category    | Party                                            |
      | location    | Aveiro                                           |
      | image       | https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQmjGpoVss7T3CbcoZ0ktRJCyONDAmMy76ofg&s |
    And I submit the item form
    Then I should see the item "JBL SPEAKER" in my items list
