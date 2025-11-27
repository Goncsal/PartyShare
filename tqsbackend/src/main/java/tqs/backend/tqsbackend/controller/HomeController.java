package tqs.backend.tqsbackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class HomeController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("profile", activeProfile);
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("version", "1.0.0-SNAPSHOT");
        model.addAttribute("applicationName", applicationName);
        return "index";
    }

    @GetMapping("/health")
    public String health(Model model) {
        model.addAttribute("status", "UP");
        model.addAttribute("profile", activeProfile);
        model.addAttribute("timestamp", LocalDateTime.now());
        return "health";
    }
}
