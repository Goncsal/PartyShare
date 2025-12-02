package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.tqsbackend.entity.Favorite;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.FavoriteRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    public void testGetFavorites() {
        // Arrange
        Long userId = 1L;
        Item item1 = new Item();
        item1.setName("Item 1");
        Item item2 = new Item();
        item2.setName("Item 2");

        Favorite fav1 = new Favorite(userId, item1);
        Favorite fav2 = new Favorite(userId, item2);

        when(favoriteRepository.findByUserId(userId)).thenReturn(Arrays.asList(fav1, fav2));

        // Act
        List<Item> result = favoriteService.getFavorites(userId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(item1, item2);
        verify(favoriteRepository).findByUserId(userId);
    }

    @Test
    public void testAddFavorite() {
        // Arrange
        Long userId = 1L;
        Long itemId = 10L;
        Item item = new Item();
        item.setId(itemId);

        when(favoriteRepository.existsByUserIdAndItemId(userId, itemId)).thenReturn(false);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        favoriteService.addFavorite(userId, itemId);

        // Assert
        verify(favoriteRepository).existsByUserIdAndItemId(userId, itemId);
        verify(itemRepository).findById(itemId);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    public void testAddFavorite_AlreadyExists() {
        // Arrange
        Long userId = 1L;
        Long itemId = 10L;

        when(favoriteRepository.existsByUserIdAndItemId(userId, itemId)).thenReturn(true);

        // Act
        favoriteService.addFavorite(userId, itemId);

        // Assert
        verify(favoriteRepository).existsByUserIdAndItemId(userId, itemId);
        verify(itemRepository, never()).findById(anyLong());
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    public void testRemoveFavorite() {
        // Arrange
        Long userId = 1L;
        Long itemId = 10L;

        // Act
        favoriteService.removeFavorite(userId, itemId);

        // Assert
        verify(favoriteRepository).deleteByUserIdAndItemId(userId, itemId);
    }
}
