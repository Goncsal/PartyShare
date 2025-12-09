package tqs.backend.tqsbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.entity.ReportState;
import tqs.backend.tqsbackend.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportRestController {

    private final ReportService reportService;

    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/new")
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        try {
            Report created = reportService.createReport(report.getSenderId(), report.getTitle(),
                    report.getDescription());
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/state")
    public ResponseEntity<Void> updateReportState(@PathVariable Long id) {
        if (reportService.updateReportState(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/state/{state}")
    public List<Report> getReportsByState(@PathVariable ReportState state) {
        return reportService.getReportsByState(state);
    }

    @GetMapping("/sender/{senderId}")
    public List<Report> getReportsBySender(@PathVariable Long senderId) {
        return reportService.getReportsBySenderId(senderId);
    }
}
