package tqs.backend.tqsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.backend.tqsbackend.service.UserService;
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final UserService userService;
    private final CategoryService categoryService;
    // private final ItemService itemService; // If we want item stats

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        // For now, just return counts if services support it, or simple placeholders
        // Since services don't have count methods exposed yet, we'll just return category count
        stats.put("categoryCount", categoryService.getAllCategories().size());
        // stats.put("userCount", userService.getAllUsers().size()); // If available
        return ResponseEntity.ok(stats);
    }
}
