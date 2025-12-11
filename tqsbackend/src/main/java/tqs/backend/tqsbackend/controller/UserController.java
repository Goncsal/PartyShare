package tqs.backend.tqsbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import tqs.backend.tqsbackend.dto.UserRegistrationDto;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "users/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model,
            RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
        if (userService.authenticate(email, password)) {
            User user = userService.getUserByEmail(email).orElseThrow();
            session.invalidate();
            HttpSession newSession = request.getSession(true);

            newSession.setAttribute("userId", user.getId());
            newSession.setAttribute("userRole", user.getRole());
            newSession.setAttribute("userName", user.getName());

            redirectAttributes.addFlashAttribute("success", "Login successful!");
            if (user.getRole() == tqs.backend.tqsbackend.entity.UserRoles.ADMIN) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/items/search";
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "users/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out.");
        return "redirect:/users/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "users/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") UserRegistrationDto user,
            RedirectAttributes redirectAttributes, Model model) {
        try {
            userService.registerUser(user.getName(), user.getEmail(), user.getPassword(), user.getRole());
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/users/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "users/register";
        }
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        // Placeholder for profile logic
        return "users/profile";
    }
}