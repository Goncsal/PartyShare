package tqs.backend.tqsbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/items/search";
    }

    /**
     * Test endpoint to verify Sentry error tracking is working.
     * Visit /sentry-test to trigger a test error.
     * TODO: Remove this endpoint after verifying Sentry works.
     */
    @GetMapping("/sentry-test")
    public String testSentry() {
        throw new RuntimeException("Sentry test error - please ignore!");
    }
}
