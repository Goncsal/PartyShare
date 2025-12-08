package tqs.backend.tqsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.FavoriteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("")
    public String getFavorites(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        List<Item> favorites = favoriteService.getFavorites(userId);
        model.addAttribute("favorites", favorites);
        model.addAttribute("userId", userId);
        return "favorites";
    }

    @PostMapping("/{itemId}")
    public String addFavorite(@PathVariable Long itemId,
            HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        favoriteService.addFavorite(userId, itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item added to favorites!");

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/items/search");
    }

    @PostMapping("/{itemId}/remove")
    public String removeFavorite(@PathVariable Long itemId,
            HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        favoriteService.removeFavorite(userId, itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from favorites!");

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/favorites");
    }
}
