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

    /**
     * Get all items for the logged-in owner
     */
    @GetMapping("/items")
    public ResponseEntity<?> getOwnerItems(HttpSession session) {
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

        List<Item> items = itemService.findByOwnerId(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Activate an item
     */
    @PatchMapping("/items/{id}/activate")
    public ResponseEntity<?> activateItem(@PathVariable Long id, HttpSession session) {
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

        try {
            Item item = itemService.activateItem(id, userId);
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
    public ResponseEntity<?> deactivateItem(@PathVariable Long id, HttpSession session) {
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

        try {
            Item item = itemService.deactivateItem(id, userId);
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
    public ResponseEntity<?> getItemRatings(@PathVariable Long id, HttpSession session) {
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
    public ResponseEntity<?> deleteItem(@PathVariable Long id, HttpSession session) {
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

        try {
            itemService.deleteItem(id, userId);
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
    public ResponseEntity<?> createDamageReport(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> requestBody,
            HttpSession session) {

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

        // Validate booking exists
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Booking not found"));
        }

        Booking booking = bookingOpt.get();

        // Validate booking belongs to this owner
        if (!booking.getItem().getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Booking does not belong to this owner"));
        }

        // Validate booking is in the past
        if (booking.getEndDate().isAfter(LocalDate.now()) || booking.getEndDate().equals(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Cannot report damage for ongoing or future rentals"));
        }

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
}
