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
    private final tqs.backend.tqsbackend.service.ReportService reportService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("categoryCount", categoryService.getAllCategories().size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports")
    public ResponseEntity<java.util.List<tqs.backend.tqsbackend.entity.Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @org.springframework.web.bind.annotation.PostMapping("/users/{id}/activate")
    public ResponseEntity<Void> activateUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        if (userService.activateUser(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/users/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        if (userService.deactivateUser(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
