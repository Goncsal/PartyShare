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

    public AdminController(UserService userService) {
        this.userService = userService;
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
}
