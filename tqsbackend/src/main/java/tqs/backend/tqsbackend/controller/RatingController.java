package tqs.backend.tqsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.service.RatingService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/new")
    public String createRating(
            @RequestParam Long senderId,
            @RequestParam RatingType ratingType,
            @RequestParam Long ratedId,
            @RequestParam Integer rate,
            @RequestParam(required = false) String comment,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            ratingService.createRating(senderId, ratingType, ratedId, rate, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Rating submitted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/items/search");
    }
}
