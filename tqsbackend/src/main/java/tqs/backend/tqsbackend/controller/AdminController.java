package tqs.backend.tqsbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import tqs.backend.tqsbackend.entity.UserRoles;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final tqs.backend.tqsbackend.service.CategoryService categoryService;
    private final tqs.backend.tqsbackend.service.ReportService reportService;

    public AdminController(UserService userService, tqs.backend.tqsbackend.service.CategoryService categoryService, tqs.backend.tqsbackend.service.ReportService reportService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        UserRoles role = (UserRoles) session.getAttribute("userRole");
        if (role != UserRoles.ADMIN) {
            return "redirect:/users/login";
        }
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(HttpSession session, Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRoles role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        UserRoles userRole = (UserRoles) session.getAttribute("userRole");
        if (userRole != UserRoles.ADMIN) {
            return "redirect:/users/login";
        }

        List<User> users = userService.searchUsers(keyword, role, startDate, endDate);
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/categories")
    public String categories(HttpSession session, Model model) {
        UserRoles role = (UserRoles) session.getAttribute("userRole");
        if (role != UserRoles.ADMIN) {
            return "redirect:/users/login";
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @org.springframework.web.bind.annotation.PostMapping("/categories")
    public String createCategory(@RequestParam String name, HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        UserRoles role = (UserRoles) session.getAttribute("userRole");
        if (role != UserRoles.ADMIN) {
            return "redirect:/users/login";
        }

        try {
            categoryService.createCategory(name);
            redirectAttributes.addFlashAttribute("success", "Category created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        UserRoles role = (UserRoles) session.getAttribute("userRole");
        if (role != UserRoles.ADMIN) {
            return "redirect:/users/login";
        }
        model.addAttribute("reports", reportService.getAllReports());
        return "admin/reports";
    }
}
