# EPIC 1 — Renter Experience
*(Everything the Renter can do in the platform)*

---

## US 1.1 — Search Items  
**As a Renter**, I want to search items by keywords so I can quickly find what I need.

### Scenarios (Gherkin)

#### Scenario 1: Successful search
Given the renter is on the search page  
When they search for "Generator"  
Then the system must display a list of items matching the keyword

#### Scenario 2: No results found
Given the renter is on the search page  
When they search for "NonExistentItem"  
Then the system must display the message "No items found"

#### Scenario 3: Partial match search
Given items exist with similar names  
When the renter searches using a partial term  
Then all relevant partial matches must be shown


---

## US 1.2 — Filter Results  
**As a Renter**, I want to filter search results by category, price, rating, and location.

### Scenarios (Gherkin)

#### Scenario 1: Filter by category
Given items exist in multiple categories  
When the renter selects the "Lighting" category  
Then the system must show only items in that category

#### Scenario 2: Filter by price range
Given items have different price ranges  
When the renter sets a price filter from €50 to €200  
Then only items priced within that range must be displayed

#### Scenario 3: Filter by minimum rating
Given items have ratings  
When the renter sets minimum rating to 4  
Then only items rated 4 or higher must be displayed

#### Scenario 4: Filter by location
Given items exist in multiple locations  
When the renter filters by "Lisbon"  
Then only items located in Lisbon must be displayed


---

## US 1.3 — View Item Details  
**As a Renter**, I want to view full item details before renting.

### Scenarios (Gherkin)

#### Scenario: Display item details
Given the renter selects an item  
When the item page loads  
Then the system must display photos, description, rules, location, reviews and availability


---

## US 1.4 — Save Favorite Items  
**As a Renter**, I want to save items as favorites.

### Scenarios (Gherkin)

#### Scenario 1: Add item to favorites
Given an item exists  
When the renter clicks "Add to favorites"  
Then the item must be added to their favorites list

#### Scenario 2: Remove item from favorites
Given an item is in the renter’s favorites  
When the renter clicks "Remove from favorites"  
Then the item must be removed

#### Scenario 3: List favorites
Given the renter has favorite items  
When they navigate to the favorites page  
Then the system must show all saved items


---

## US 1.5 — Message Owner  
**As a Renter**, I want to message the owner before renting.

### Scenarios (Gherkin)

#### Scenario: Send message
Given a conversation exists or can be created  
When the renter sends a message  
Then the message must be stored and delivered to the owner


---

## Negotiation Flow (Extension of Renting)
*(Part of US 1.6 / US 2.3)*

### Scenarios (Gherkin)

#### Scenario 1: Renter proposes price
Given a booking is in REQUESTED state  
When the renter sends a price proposal  
Then the booking enters NEGOTIATION and the proposal is visible to the owner

#### Scenario 2: Owner counter-offers
Given the owner has received a proposal  
When the owner sends a counter-offer  
Then the booking price updates to the counter-offer  
And the state becomes COUNTER_OFFER

#### Scenario 3: Renter accepts final price
Given a counter-offer exists  
When the renter accepts it  
Then the booking status becomes ACCEPTED  
And the agreedPrice is locked

#### Scenario 4: Payment enabled only after acceptance
Given a booking is not ACCEPTED  
When the renter tries to pay  
Then the system must deny the payment


---

## US 1.6 — Rent an Item  
**As a Renter**, I want to request a rental and make a payment once the price is agreed.

### Scenarios (Gherkin)

#### Scenario 1: Successful booking
Given the renter selected dates  
When the item is available  
Then the booking must be created in REQUESTED state

#### Scenario 2: Unavailable dates
Given the item is unavailable in the selected period  
When the renter requests a booking  
Then the system must return an error

#### Scenario 3: Successful payment
Given the booking is ACCEPTED  
When the renter completes payment  
Then the booking becomes CONFIRMED

#### Scenario 4: Failed payment
Given the renter attempts payment  
When the external provider declines it  
Then the system must keep the booking in ACCEPTED state  
And show an error


---

## US 1.7 — Cancel Rental  
**As a Renter**, I want to cancel my rental according to the rules.

### Scenarios (Gherkin)

#### Scenario 1: Cancellation within policy
Given a booking is CONFIRMED  
And the renter cancels within allowed time  
When they cancel  
Then the system must issue a refund  
And set booking to CANCELLED

#### Scenario 2: Cancellation outside policy
Given a booking is CONFIRMED  
When the renter cancels too late  
Then the system must deny refund  
But set the booking to CANCELLED


---

## US 1.8 — View My Bookings  
**As a Renter**, I want to see all my bookings.

### Scenarios (Gherkin)

#### Scenario: Display bookings
Given the renter has active and past bookings  
When they open the bookings page  
Then the system must show them grouped by status


---

## US 1.9 — Rate Owner  
### Scenarios (Gherkin)

#### Scenario: Submit owner rating
Given a booking is completed  
When the renter submits a rating  
Then the owner rating must update


---

## US 1.10 — Rate Item  
### Scenarios (Gherkin)

#### Scenario: Submit item rating
Given the renter completed the booking  
When they submit a rating  
Then the item’s average rating must update


---

## US 1.11 — Report Item/Owner Issue  
### Scenarios (Gherkin)

#### Scenario: Submit report
Given the renter had a problem  
When they create a report  
Then the system must store it  
And notify the admin

---

# EPIC 2 — Item Owner Experience
*(Everything Owners do to manage items and rentals)*

---

## US 2.1 — Add New Item  
### Scenarios (Gherkin)

#### Scenario: Successful item creation
Given the owner fills all required fields  
When they submit the item  
Then the item must be created and marked ACTIVE


---

## US 2.2 — Edit/Activate/Deactivate Item  
### Scenarios (Gherkin)

#### Scenario 1: Edit item
Given an item exists  
When the owner edits fields  
Then the item updates successfully

#### Scenario 2: Deactivate item
When the owner deactivates the item  
Then it must disappear from search

#### Scenario 3: Reactivate item
When owner reactivates  
Then item returns to search results


---

## US 2.3 — View Booking Requests  
### Scenarios (Gherkin)

#### Scenario 1: Accept booking
Given a booking is REQUESTED  
When the owner accepts it  
Then it becomes ACCEPTED

#### Scenario 2: Decline booking
When the owner declines  
Then booking becomes CANCELLED

#### Scenario 3: Send counter-offer
When the owner submits a new price  
Then the booking’s agreedPrice updates  
And the state becomes COUNTER_OFFER


---

## US 2.4 — Owner Dashboard  
### Scenarios (Gherkin)

#### Scenario: Display dashboard
Given the owner has upcoming and past rentals  
When they open dashboard  
Then all relevant metrics must appear


---

## US 2.5 — Message Renter  
### Scenarios (Gherkin)

#### Scenario: Owner sends message
Given a conversation exists  
When the owner sends a message  
Then it must be delivered and saved


---

## US 2.6 — Item Returned Damaged  
### Scenarios (Gherkin)

#### Scenario: Owner reports damage
Given an item was returned  
When the owner files a report  
Then the system must store it  
And notify the admin


---

## US 2.7 — Rate Renter  
### Scenarios (Gherkin)

#### Scenario: Submit renter rating
Given the booking is completed  
When the owner rates the renter  
Then the renter’s score must update


---

# EPIC 3 — Admin & Platform Management

---

## US 3.1 — Manage Users  
### Scenarios (Gherkin)

#### Scenario 1: Disable user
When the admin disables a user  
Then the user must not be able to log in

#### Scenario 2: Reactivate user
When the admin reactivates  
Then the user regains access


---

## US 3.2 — Manage Item Categories  
### Scenarios (Gherkin)

#### Scenario 1: Create category
When the admin creates a category  
Then it must appear in the item form

#### Scenario 2: Update category
When the admin edits a category  
Then changes must reflect immediately


---

## US 3.3 — View Reports / Issues  
### Scenarios (Gherkin)

#### Scenario 1: List all reports
When admin opens reports page  
Then all reports must be visible

#### Scenario 2: Change report status
When admin sets status to IN_REVIEW or RESOLVED  
Then the change must be saved


---

## US 3.4 — View Metrics Dashboard  
### Scenarios (Gherkin)

#### Scenario: Display metrics
Given the system has data  
When admin opens dashboard  
Then stats (bookings, users, items, volume) must appear
