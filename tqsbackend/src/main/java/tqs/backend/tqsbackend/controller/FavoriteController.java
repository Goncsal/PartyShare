package tqs.backend.tqsbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.FavoriteService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/{userId}")
    public String getFavorites(@PathVariable Long userId, Model model) {
        List<Item> favorites = favoriteService.getFavorites(userId);
        model.addAttribute("favorites", favorites);
        model.addAttribute("userId", userId);
        return "favorites";
    }

    @PostMapping("/{userId}/{itemId}")
    public String addFavorite(@PathVariable Long userId, @PathVariable Long itemId,
            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        favoriteService.addFavorite(userId, itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item added to favorites!");

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/items/search");
    }

    @PostMapping("/{userId}/{itemId}/remove")
    public String removeFavorite(@PathVariable Long userId, @PathVariable Long itemId,
            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        favoriteService.removeFavorite(userId, itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from favorites!");

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/favorites/" + userId);
    }
}
