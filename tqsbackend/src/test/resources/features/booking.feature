Feature: Booking
  As a renter
  I want to book items
  So that I can use them

  Scenario: Successful booking creation
    Given a renter exists with email "renter@email.com"
    And an item exists with name "Camera", price 100.0, and owner "owner@email.com"
    When I book the item "Camera" from "2026-01-01" to "2026-01-05"
    Then the booking should be created successfully
    And the total price should be 400.0

  Scenario: Booking fails due to unavailable dates
    Given a renter exists with email "renter2@email.com"
    And an item exists with name "Tent", price 20.0, and owner "owner2@email.com"
    And the item "Tent" is already booked from "2026-02-01" to "2026-02-05"
    When I try to book the item "Tent" from "2026-02-03" to "2026-02-07"
    Then the booking should fail with error "Item not available for the selected dates"
