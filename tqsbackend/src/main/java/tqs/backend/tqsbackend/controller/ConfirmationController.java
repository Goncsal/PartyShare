package tqs.backend.tqsbackend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.ConfirmationService;

@RestController
@RequestMapping("/api/bookings")
public class ConfirmationController {

    private final ConfirmationService confirmationService;

    public ConfirmationController(ConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @PostMapping("/{bookingId}/confirm-return")
    public ResponseEntity<String> confirmReturn(@PathVariable Long bookingId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        UserRoles role = (UserRoles) session.getAttribute("userRole");

        if (userId == null || role != UserRoles.RENTER) {
            return ResponseEntity.status(403).body("Only renters can confirm returns");
        }

        boolean success = confirmationService.confirmByRenter(bookingId, userId);
        if (success) {
            return ResponseEntity.ok("Return confirmed successfully");
        }
        return ResponseEntity.badRequest().body("Failed to confirm return");
    }

    @PostMapping("/{bookingId}/confirm-received")
    public ResponseEntity<String> confirmReceived(@PathVariable Long bookingId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        UserRoles role = (UserRoles) session.getAttribute("userRole");

        if (userId == null || role != UserRoles.OWNER) {
            return ResponseEntity.status(403).body("Only owners can confirm receipt");
        }

        boolean success = confirmationService.confirmByOwner(bookingId, userId);
        if (success) {
            return ResponseEntity.ok("Receipt confirmed successfully");
        }
        return ResponseEntity.badRequest().body("Failed to confirm receipt");
    }

    @GetMapping("/{bookingId}/confirmation-status")
    public ResponseEntity<ConfirmationStatus> getConfirmationStatus(@PathVariable Long bookingId) {
        boolean fullyConfirmed = confirmationService.isFullyConfirmed(bookingId);
        return ResponseEntity.ok(new ConfirmationStatus(fullyConfirmed));
    }

    public record ConfirmationStatus(boolean fullyConfirmed) {}
}
