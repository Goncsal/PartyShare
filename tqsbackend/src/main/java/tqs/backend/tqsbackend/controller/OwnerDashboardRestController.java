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

    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    private final ItemService itemService;
    private final RatingService ratingService;
    private final UserService userService;
    private final ReportService reportService;
    private final BookingRepository bookingRepository;

    private ResponseEntity<Object> validateOwnerSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR_KEY, "User not logged in"));
        }

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRole() != UserRoles.OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(ERROR_KEY, "User is not an owner"));
        }
        return null;
    }

    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    private ResponseEntity<Object> validateBooking(Long bookingId) {
        if (bookingRepository.findById(bookingId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, "Booking not found"));
        }
        return null;
    }

    private ResponseEntity<Object> validateBookingOwnership(Booking booking, Long userId) {
        if (!booking.getItem().getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(ERROR_KEY, "Booking does not belong to this owner"));
        }
        return null;
    }

    private ResponseEntity<Object> validatePastBooking(Booking booking, String errorMessage) {
        if (booking.getEndDate().isAfter(LocalDate.now()) || booking.getEndDate().equals(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, errorMessage));
        }
        return null;
    }

    private Booking validateBookingForOwner(Long bookingId, HttpSession session, boolean mustBePast,
            String pastErrorMessage) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null) {
            throw new ValidationException(validationError);
        }

        validationError = validateBooking(bookingId);
        if (validationError != null) {
            throw new ValidationException(validationError);
        }

        Long userId = getUserId(session);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        validationError = validateBookingOwnership(booking, userId);
        if (validationError != null) {
            throw new ValidationException(validationError);
        }

        if (mustBePast) {
            validationError = validatePastBooking(booking, pastErrorMessage);
            if (validationError != null) {
                throw new ValidationException(validationError);
            }
        }

        return booking;
    }

    private static class ValidationException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final transient ResponseEntity<Object> response;

        ValidationException(ResponseEntity<Object> response) {
            this.response = response;
        }

        ResponseEntity<Object> getResponse() {
            return response;
        }
    }

    private ResponseEntity<Object> handleItemOperation(Long itemId, HttpSession session,
            java.util.function.BiFunction<Long, Long, Item> operation) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        try {
            Item item = operation.apply(itemId, getUserId(session));
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    private Integer extractAndValidateRate(Map<String, Object> requestBody) {
        try {
            Integer rate = (Integer) requestBody.get("rate");
            if (rate == null) {
                throw new IllegalArgumentException("Rating is required");
            }
            return rate;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid rating format");
        }
    }

    @GetMapping("/items")
    public ResponseEntity<Object> getOwnerItems(HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        List<Item> items = itemService.findByOwnerId(getUserId(session));
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/items/{id}/activate")
    public ResponseEntity<Object> activateItem(@PathVariable Long id, HttpSession session) {
        return handleItemOperation(id, session, itemService::activateItem);
    }

    @PatchMapping("/items/{id}/deactivate")
    public ResponseEntity<Object> deactivateItem(@PathVariable Long id, HttpSession session) {
        return handleItemOperation(id, session, itemService::deactivateItem);
    }

    @GetMapping("/items/{id}/ratings")
    public ResponseEntity<Object> getItemRatings(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        Long userId = getUserId(session);

        Item item = itemService.getItemById(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(ERROR_KEY, "Item not found"));
        }

        if (!item.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(ERROR_KEY, "Item does not belong to this owner"));
        }

        List<Rating> ratings = ratingService.getRatingByRatedInfo(RatingType.PRODUCT, id);
        return ResponseEntity.ok(ratings);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Object> validationError = validateOwnerSession(session);
        if (validationError != null)
            return validationError;

        try {
            itemService.deleteItem(id, getUserId(session));
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Item deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @PostMapping("/bookings/{bookingId}/damage-report")
    public ResponseEntity<Object> createDamageReport(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> requestBody,
            HttpSession session) {

        try {
            Booking booking = validateBookingForOwner(bookingId, session, true,
                    "Cannot report damage for ongoing or future rentals");

            String damageDescription = requestBody.get("damageDescription");
            if (damageDescription == null || damageDescription.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(ERROR_KEY, "Damage description is required"));
            }

            String reportTitle = String.format("Damage Report - Booking #%d - %s",
                    bookingId, booking.getItem().getName());

            String reportDescription = String.format("""
                    Damage Report Details:
                    - Booking ID: %d
                    - Item: %s (ID: %d)
                    - Renter ID: %d
                    - Rental Period: %s to %s
                    - Total Price: €%.2f

                    Damage Description:
                    %s""",
                    booking.getId(),
                    booking.getItem().getName(),
                    booking.getItem().getId(),
                    booking.getRenterId(),
                    booking.getStartDate(),
                    booking.getEndDate(),
                    booking.getTotalPrice(),
                    damageDescription);

            Report report = reportService.createReport(getUserId(session), reportTitle, reportDescription);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            MESSAGE_KEY, "Damage report created successfully",
                            "reportId", report.getId()));
        } catch (ValidationException e) {
            return e.getResponse();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @GetMapping("/bookings/{bookingId}/renter-rating")
    public ResponseEntity<Object> getRenterRating(
            @PathVariable Long bookingId,
            HttpSession session) {

        try {
            Booking booking = validateBookingForOwner(bookingId, session, false, null);

            Optional<Rating> ratingOpt = ratingService.getRatingBySenderIdAndRatedInfo(
                    getUserId(session), RatingType.RENTER, booking.getRenterId());

            if (ratingOpt.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "rating", ratingOpt.get()));
            } else {
                return ResponseEntity.ok(Map.of("exists", false));
            }
        } catch (ValidationException e) {
            return e.getResponse();
        }
    }

    @PostMapping("/bookings/{bookingId}/rate-renter")
    public ResponseEntity<Object> rateRenter(
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {

        try {
            Booking booking = validateBookingForOwner(bookingId, session, true,
                    "Cannot rate renter for ongoing or future rentals");

            Integer rate = extractAndValidateRate(requestBody);
            String comment = (String) requestBody.get("comment");

            Rating rating = ratingService.createRating(
                    getUserId(session),
                    RatingType.RENTER,
                    booking.getRenterId(),
                    rate,
                    comment);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            MESSAGE_KEY, "Renter rated successfully",
                            "ratingId", rating.getId()));
        } catch (ValidationException e) {
            return e.getResponse();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }
}
