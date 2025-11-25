# EPIC 1 — Renter Experience
*(Everything the Renter can do in the platform)*

This epic includes searching, filtering, item details, favorites, messaging, price negotiation, bookings, payments, ratings and reporting issues.

---

## 1. Search & Browse Items

### US 1.1 — Search Items  
**As a Renter**, I want to search items by keywords.

**Tests:**  
- Search “Generator” → List of matching items  
- Search “NonExistentItem” → “No items found”  
- Search is case-insensitive  
- Search returns items sorted by relevance

---

### US 1.2 — Filter Results  
**As a Renter**, I want to filter results by category, price, rating, and location.

**Tests:**  
- Filter by category = Lighting → Only Lighting items shown  
- Filter by price range €50–€200 → Items within range shown  
- Filter by minimum rating = 4 → Items rated ≥4 shown  
- Filter by location = Lisbon → Items in Lisbon shown  
- Combining filters narrows results correctly  
- Applying no filters returns all items  

---

### US 1.3 — View Item Details  
**As a Renter**, I want to see full item details (photos, description, rules, reviews, availability, location).

**Tests:**  
- Item photos load correctly  
- Availability calendar is displayed  
- Location displayed accurately  
- Rules shown  
- Reviews section displays latest reviews  
- Owner details shown  

---

## 2. Favorites

### US 1.4 — Save Favorite Items  
Add, remove and list favorites.

**Tests:**  
- Add item to favorites → Marked as favorite  
- Remove item from favorites → Removed successfully  
- List favorites shows correct items  
- Favorite status persists across sessions  

---

## 3. Messaging & Pre-Rental Negotiation

### US 1.5 — Message Owner  
**As a Renter**, I want to message the owner before renting.

**Tests:**  
- Message delivered and stored  
- Conversation is created if not existing  
- Messages appear in correct order  
- Owner receives notification  

---

### Negotiation Flow (Extension to renting)
- Renter proposes a price via chat  
- Owner responds with a counter-offer  
- If both agree → booking moves to `ACCEPTED` with updated `agreedPrice`  
- Payment only enabled once price is accepted

**Tests:**  
- Renter can send suggested price  
- Owner can send counter-offer  
- agreedPrice updates correctly  
- Booking status transitions: REQUESTED → COUNTER_OFFER → ACCEPTED  
- Payment blocked before ACCEPTED  
- Payment enabled after ACCEPTED  

---

## 4. Booking & Payment

### US 1.6 — Rent an Item  
**As a Renter**, I want to select rental dates, negotiate if needed, accept the price and pay.

**Flow:**  
1. Booking requested  
2. Owner accepts or counter-offers  
3. Renter accepts final price  
4. Payment is made (external provider like Stripe)

**Tests:**  
- Booking created with correct dates  
- Total price calculated correctly  
- Attempting to book unavailable dates → error  
- Booking cannot be confirmed without payment  
- Payment success marks booking as CONFIRMED  
- Payment failure keeps booking as ACCEPTED  

---

### US 1.7 — Cancel Rental  
**As a Renter**, I want to cancel my rental according to rules.

**Tests:**  
- Cancel within allowed window → refund issued  
- Cancel outside policy → user notified of penalty  
- Booking status updated to CANCELLED  
- Refund reflected in payment system  
- Notification sent to owner  

---

### US 1.8 — View My Bookings  
View active and past bookings.

**Tests:**  
- Active bookings listed correctly  
- Past bookings listed correctly  
- Booking status visible  
- Correct sorting (active first, then past)  

---

## 5. Post-Rental

### US 1.9 — Rate Owner  
Submit a rating for the owner.

**Tests:**  
- Rating saved correctly  
- Owner average rating updated  
- Prevent duplicate rating for same booking  

---

### US 1.10 — Rate Item  
Submit a rating for the item.

**Tests:**  
- Rating saved  
- Item average rating updated  
- Cannot rate an item twice for same booking  

---

## 6. Report Issues

### US 1.11 — Report Item/Owner Issue  
**As a Renter**, I want to report any problem with an item or another user.  
Reports are handled by the Admin.

**Tests:**  
- Report created with correct type (item or user)  
- Report linked to booking/item/user  
- Report status = OPEN  
- Admin sees report in dashboard  
- User receives confirmation of submission  

---

# EPIC 2 — Item Owner Experience
*(Everything Owners do to manage items and rentals)*

---

## 1. Item Management

### US 2.1 — Add New Item  
Add items with photos, rules, pricing.

**Tests:**  
- Item created successfully  
- Photos saved  
- Category selection works  
- Price stored correctly  

---

### US 2.2 — Edit/Activate/Deactivate Item  
Update information and control availability.

**Tests:**  
- Edit updates fields correctly  
- Deactivating hides item from search  
- Activating restores visibility  
- Availability calendar updates  

---

## 2. Booking Management

### US 2.3 — View Booking Requests  
Accept, decline or send counter-offers.

**Tests:**  
- Booking appears in list  
- Owner can accept → status = ACCEPTED  
- Owner can decline → status = CANCELLED  
- Owner can make counter-offer → updated agreedPrice  
- Renter notified  

---

### US 2.4 — Owner Dashboard  
View upcoming rentals, past rentals and item activity.

**Tests:**  
- Upcoming bookings correct  
- Past bookings correct  
- Totals displayed  
- Item activity metrics shown  

---

## 3. Communication

### US 2.5 — Message Renter  
Respond to renter questions and negotiate price.

**Tests:**  
- Messages stored correctly  
- Renter receives notification  
- Negotiation messages update booking when applicable  

---

## 4. Quality & Issues

### US 2.6 — Item Returned Damaged  
Owner can create a report.

**Tests:**  
- Report created  
- Linked to booking and renter  
- Admin sees it  
- Status initially OPEN  

---

### US 2.7 — Rate Renter  
Owner reviews renter behavior.

**Tests:**  
- Rating saved  
- Renter average rating updated  
- Prevent duplicate rating  

---

# EPIC 3 — Admin & Platform Management
*(Admin-only operations ensuring platform governance)*

---

## 1. User Administration

### US 3.1 — Manage Users  
Activate, deactivate or review user accounts.

**Tests:**  
- Admin can disable user  
- Disabled user cannot log in  
- Admin can reactivate account  
- User list displays correct status  

---

## 2. Category Management

### US 3.2 — Manage Item Categories  
Add/edit categories (Lighting, Audio, etc.).

**Tests:**  
- Create category  
- Update category  
- Prevent duplicate names  
- Category deletion prevented if items exist (depending on policy)  

---

## 3. Reports Oversight

### US 3.3 — View Reports / Issues  
Admin examines:
- item problems  
- user behavior issues  
- damage disputes  

Admin updates report state:
- OPEN  
- IN_REVIEW  
- RESOLVED  

**Tests:**  
- Admin sees full report list  
- Can filter by status/type  
- Changing status updates correctly  
- Report history maintained  

---

## 4. Platform KPIs

### US 3.4 — View Metrics Dashboard  
Track:
- number of bookings  
- active users  
- item count  
- business performance  

**Tests:**  
- Metrics display correct numbers  
- Data updates dynamically  
- Graphs/charts load successfully  

