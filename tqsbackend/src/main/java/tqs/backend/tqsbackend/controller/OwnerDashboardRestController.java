package tqs.backend.tqsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.RatingService;
import tqs.backend.tqsbackend.service.UserService;
import tqs.backend.tqsbackend.service.ReportService;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/owner/dashboard")
@RequiredArgsConstructor
public class OwnerDashboardRestController {

    private final ItemService itemService;
    private final RatingService ratingService;
    private final UserService userService;
    private final ReportService reportService;
    private final BookingRepository bookingRepository;

    // ========== HELPER METHODS TO REDUCE DUPLICATION ==========

    /**
     * Validates that the user is logged in and has OWNER role
     * 
     * @return ResponseEntity with error if validation fails, null if successful
     */
    private ResponseEntity<Object> validateOwnerSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not logged in"));
        }

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRole() != UserRoles.OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "User is not an owner"));
        }
        return null;
    }

    /**
     * Gets the current user ID from session (assumes validation already done)
     */
    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    /**
     * Validates that a booking exists
     * 
     * @return ResponseEntity with error if validation fails, null if successful
     */
    private ResponseEntity<Object> validateBooking(Long bookingId) {
        if (bookingRepository.findById(bookingId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Booking not found"));
        }
        return null;
    }

    /**
     * Validates that the booking belongs to the owner
     * 
     * @return ResponseEntity with error if validation fails, null if successful
     */
    private ResponseEntity<Object> validateBookingOwnership(Booking booking, Long userId) {
        if (!booking.getItem().getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Booking does not belong to this owner"));
        }
        return null;
    }

    /**
     * Validates that the booking is in the past
     * 
     * @return ResponseEntity with error if validation fails, null if successful
     */
    private ResponseEntity<Object> validatePastBooking(Booking booking, String errorMessage) {
        if (booking.getEndDate().isAfter(LocalDate.now()) || booking.getEndDate().equals(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", errorMessage));
        }
        return null;
    }

    /**
     * Get all items for the logged-in owner
     */
    @GetMapping("/items")
    public ResponseEntity<Object> getOwnerItems(HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        List<Item> items = itemService.findByOwnerId(getUserId(session));
        return ResponseEntity.ok(items);
    }

    /**
     * Activate an item
     */
    @PatchMapping("/items/{id}/activate")
    public ResponseEntity<Object> activateItem(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        try {
            Item item = itemService.activateItem(id, getUserId(session));
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deactivate an item
     */
    @PatchMapping("/items/{id}/deactivate")
    public ResponseEntity<Object> deactivateItem(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        try {
            Item item = itemService.deactivateItem(id, getUserId(session));
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all ratings for a specific item owned by the logged-in owner
     */
    @GetMapping("/items/{id}/ratings")
    public ResponseEntity<Object> getItemRatings(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        Long userId = getUserId(session);

        // Verify the item belongs to this owner
        Item item = itemService.getItemById(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Item not found"));
        }

        if (!item.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Item does not belong to this owner"));
        }

        List<Rating> ratings = ratingService.getRatingByRatedInfo(RatingType.PRODUCT, id);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Delete an item
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        try {
            itemService.deleteItem(id, getUserId(session));
            return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a damage report for a past rental
     */
    @PostMapping("/bookings/{bookingId}/damage-report")
    public ResponseEntity<Object> createDamageReport(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> requestBody,
            HttpSession session) {

        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        validationError = validateBooking(bookingId);
        if (validationError != null)
            return validationError;

        Long userId = getUserId(session);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        validationError = validateBookingOwnership(booking, userId);
        if (validationError != null)
            return validationError;

        validationError = validatePastBooking(booking, "Cannot report damage for ongoing or future rentals");
        if (validationError != null)
            return validationError;

        // Get damage description from request
        String damageDescription = requestBody.get("damageDescription");
        if (damageDescription == null || damageDescription.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Damage description is required"));
        }

        // Create report with formatted title and description
        String reportTitle = String.format("Damage Report - Booking #%d - %s",
                bookingId, booking.getItem().getName());

        String reportDescription = String.format(
                "Damage Report Details:\n" +
                        "- Booking ID: %d\n" +
                        "- Item: %s (ID: %d)\n" +
                        "- Renter ID: %d\n" +
                        "- Rental Period: %s to %s\n" +
                        "- Total Price: €%.2f\n\n" +
                        "Damage Description:\n%s",
                booking.getId(),
                booking.getItem().getName(),
                booking.getItem().getId(),
                booking.getRenterId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalPrice(),
                damageDescription);

        try {
            Report report = reportService.createReport(userId, reportTitle, reportDescription);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Damage report created successfully",
                            "reportId", report.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookings/{bookingId}/renter-rating")
    public ResponseEntity<Object> getRenterRating(
            @PathVariable Long bookingId,
            HttpSession session) {

        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        validationError = validateBooking(bookingId);
        if (validationError != null)
            return validationError;

        Long userId = getUserId(session);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        validationError = validateBookingOwnership(booking, userId);
        if (validationError != null)
            return validationError;

        // Check if a rating exists
        Optional<Rating> ratingOpt = ratingService.getRatingBySenderIdAndRatedInfo(
                userId, RatingType.RENTER, booking.getRenterId());

        if (ratingOpt.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "rating", ratingOpt.get()));
        } else {
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }

    /**
     * Create a renter rating for a past rental
     */
    @PostMapping("/bookings/{bookingId}/rate-renter")
    public ResponseEntity<Object> rateRenter(
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {

        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        validationError = validateBooking(bookingId);
        if (validationError != null)
            return validationError;

        Long userId = getUserId(session);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        validationError = validateBookingOwnership(booking, userId);
        if (validationError != null)
            return validationError;

        validationError = validatePastBooking(booking, "Cannot rate renter for ongoing or future rentals");
        if (validationError != null)
            return validationError;

        // Get rating details from request
        Integer rate;
        try {
            rate = (Integer) requestBody.get("rate");
            if (rate == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Rating is required"));
            }
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid rating format"));
        }

        String comment = (String) requestBody.get("comment");

        try {
            Rating rating = ratingService.createRating(
                    userId,
                    RatingType.RENTER,
                    booking.getRenterId(),
                    rate,
                    comment);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Renter rated successfully",
                            "ratingId", rating.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
