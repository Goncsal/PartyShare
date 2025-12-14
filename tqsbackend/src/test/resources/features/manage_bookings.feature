@manage_bookings
Feature: Manage Bookings Journey

  Scenario: Owner accepts a booking request
    Given I am a registered user with email "owner@email.com"
    And a user exists with email "renter@email.com"
    And the following items exist:
      | name      | price | category |
      | Party Hat | 50.0  | Party    |
    And a pending booking request exists for "Party Hat" from "renter@email.com"
    And I login as an owner with email "owner@email.com" and password "password"
    And I navigate to the dashboard
    When I click on the dashboard link "My BookingRequests"
    And I accept the booking request for "Party Hat"
    Then the booking request for "Party Hat" should be "ACCEPTED"
