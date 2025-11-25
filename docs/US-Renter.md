# Features
## 1. Search and Browse Items
## 2. Renting Process
## 3. Post-Rental Actions

---

# 1. Search and Browse Items

## 1.1 Search Items
| Renter - Search Items
As a Renter, I want to search items by keywords to find what I'm looking for.

Test Cases:
  1. Search by keyword
       - Input: "Portable Energy Generator"
          - Expected Output: List of items related to the keyword
       1. Search with no results
       - Input: "NonExistentItem"
          - Expected Output: "No items found" message
     
## 1.2 Filter Search Results
| Renter - Filter Search Results
As a Renter, I want to filter results by category, price, rating, and location.

Test Cases:
  1. Filter by category
       - Input: Category = "Lighting"
          - Expected Output: Items in "Lighting"
       2. Filter by price range
       - Input: Price Range = "€50–€200"
          - Expected Output: Items within range
       3. Filter by rating
       - Input: Minimum rating = 4
          - Expected Output: Items rated ≥ 4
       4. Filter by location
       - Input: "Lisbon"
          - Expected Output: Items available in Lisbon
     
## 1.3 View Item Details
| Renter - View Item Details
As a Renter, I want full item details before deciding to rent.

Test Cases:
  1. View item details
       - Input: Select an item
          - Expected Output: Photos, description, rules, price, location, reviews, availability
     
## 1.4 Save Favorite Items
| Renter - Save Favorite Items
As a Renter, I want to save items as favorites to revisit and compare options.

Test Cases:
  1. Save an item
       - Input: Click "Add to Favorites"
          - Expected Output: Item stored in favorites
       2. Remove an item
       - Input: Click "Remove from Favorites"
          - Expected Output: Item removed from favorites
  3. View Favorites List
       - Input: Navigate to favorites
          - Expected Output: Display all saved items

---

# 2. Renting Process

## 2.1 Message Owner
| Renter - Message Owner
As a Renter, I want to message the owner to ask questions and discuss details before renting.

Test Cases:
  1. Send a message
     - Input: Compose message + Send
     - Expected Output: Message delivered & stored

---

## 2.2 Rent an Item
| Renter - Rent an Item
As a Renter, I want to rent an item.

Test Cases:
  1. Successful rental
       - Input: Choose dates + pay
          - Expected Output: Rental confirmed
       2. Unavailable dates
       - Input: Select blocked dates
          - Expected Output: Error message
     
---

## 2.3 Cancel a Rental
| Renter - Cancel Rental
As a Renter, I want to cancel a rental.

Test Cases:
  1. Cancel within policy
       - Input: Cancel before deadline
          - Expected Output: Rental cancelled + refund if applicable
       2. Outside policy
       - Input: Cancel after deadline
          - Expected Output: Error message
     
---

## 2.4 View Active and Past Bookings
| Renter - View Bookings
As a Renter, I want to view active and past bookings.

Test Cases:
  1. View active
       - Input: Navigate to bookings
          - Expected Output: List of active rentals
       2. View past
       - Input: Navigate to bookings
          - Expected Output: Past rentals list

---

# 3. Post-Rental Actions

## 3.1 Rate Owner
| Renter - Rate Owner
As a Renter, I want to rate the owner.

Test Cases:
  1. Submit rating
       - Input: Score fields
          - Expected Output: Rating stored
       2. View rating
       - Input: Past rentals
          - Expected Output: Display rating
     
---

## 3.2 Rate Item
| Renter - Rate Item
As a Renter, I want to rate the item after using it.

Test Cases:
  1. Submit rating
       - Input: Item quality rating
          - Expected Output: Stored
       2. View submitted rating
       - Input: Past rentals
          - Expected Output: Display rating
     
---

## 3.3 Report an Issue (Item or Owner)
| Renter - Report Issue
As a Renter, I want to report an issue related to my rental experience.

Test Cases:
  1. Report item
       - Input: Select issue + Submit
          - Expected Output: Report logged
       2. Report owner
       - Input: Select owner + Submit
          - Expected Output: Report logged
     
---

## 3.4 Contact Support
| Renter - Support Request
As a Renter, I want to contact support for help.

Test Cases:
  1. Submit request
       - Input: Fill support form
          - Expected Output: Support ticket created
       2. View support response
       - Input: Check inbox
          - Expected Output: Display response
