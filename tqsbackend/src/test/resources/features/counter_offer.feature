Feature: Counter Offer
  As an owner or renter
  I want to negotiate the price of a booking
  So that we can reach an agreement

  Background:
    Given a renter exists with email "renter@email.com"
    And an item exists with name "Guitar", price 50.0, and owner "owner@email.com"
    And I have a pending booking for "Guitar" from "2026-03-01" to "2026-03-05"

  Scenario: Owner makes a counter offer
    When the owner makes a counter offer of 60.0 for the booking
    Then the booking status should be "COUNTER_OFFER"
    And the booking daily price should be 60.0

  Scenario: Renter accepts a counter offer
    Given the owner has made a counter offer of 60.0
    When the renter accepts the counter offer
    Then the booking status should be "ACCEPTED"
    And the booking total price should be calculated based on 60.0

  Scenario: Renter declines a counter offer
    Given the owner has made a counter offer of 60.0
    When the renter declines the counter offer
    Then the booking status should be "CANCELLED"
