package tqs.backend.tqsbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Favorite;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.FavoriteRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final ItemRepository itemRepository;

    public List<Item> getFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(Favorite::getItem)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavorite(Long userId, Long itemId) {
        if (!favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
            Favorite favorite = new Favorite(userId, item);
            favoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void removeFavorite(Long userId, Long itemId) {
        favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
    }
}
