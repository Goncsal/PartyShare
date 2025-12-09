package tqs.backend.tqsbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.service.ReportService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String listReports(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        
        List<Report> reports = reportService.getReportsBySenderId(userId);
        model.addAttribute("reports", reports);
        model.addAttribute("userId", userId);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("userName", session.getAttribute("userName"));
        
        return "reports";
    }

    @PostMapping("/new")
    public String createReport(@RequestParam String title, @RequestParam String description, 
                             HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        
        try {
            reportService.createReport(userId, title, description);
            redirectAttributes.addFlashAttribute("success", "Report submitted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit report: " + e.getMessage());
        }
        
        return "redirect:/reports";
    }
}
