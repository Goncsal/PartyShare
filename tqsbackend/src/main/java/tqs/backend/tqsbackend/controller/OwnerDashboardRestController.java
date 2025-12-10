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
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.RatingService;
import tqs.backend.tqsbackend.service.UserService;

import jakarta.servlet.http.HttpSession;
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
     * Create a new rental item
     */
    @PostMapping("/items")
    public ResponseEntity<?> createItem(@RequestBody Item item, HttpSession session) {
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
            item.setOwnerId(userId);
            Item createdItem = itemService.createItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
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
}
