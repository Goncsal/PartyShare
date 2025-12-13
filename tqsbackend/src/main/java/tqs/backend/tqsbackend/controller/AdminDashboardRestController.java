package tqs.backend.tqsbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.backend.tqsbackend.dto.GlobalStatsDTO;
import tqs.backend.tqsbackend.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardRestController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardRestController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<GlobalStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(adminDashboardService.getGlobalStats());
    }
}
