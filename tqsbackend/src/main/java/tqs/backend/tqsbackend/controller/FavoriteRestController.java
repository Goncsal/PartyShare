package tqs.backend.tqsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.FavoriteService;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteRestController {

    private final FavoriteService favoriteService;

    @GetMapping("/{userId}")
    public List<Item> getFavorites(@PathVariable Long userId) {
        return favoriteService.getFavorites(userId);
    }

    @PostMapping("/{userId}/{itemId}")
    public void addFavorite(@PathVariable Long userId, @PathVariable Long itemId) {
        favoriteService.addFavorite(userId, itemId);
    }

    @DeleteMapping("/{userId}/{itemId}")
    public void removeFavorite(@PathVariable Long userId, @PathVariable Long itemId) {
        favoriteService.removeFavorite(userId, itemId);
    }
}
