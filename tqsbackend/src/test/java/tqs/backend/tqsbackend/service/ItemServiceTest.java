package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.ItemRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    public void testSearchItems() {
        // Arrange
        Item item1 = new Item("Lamp", "Desk Lamp", 20.0, new Category("Lighting"), 4.5, "Lisbon");
        Item item2 = new Item("Chair", "Office Chair", 50.0, new Category("Furniture"), 4.0, "Porto");
        List<Item> expectedItems = Arrays.asList(item1, item2);

        when(itemRepository.findAll(Mockito.<Specification<Item>>any())).thenReturn(expectedItems);

        // Act
        List<Item> result = itemService.searchItems("Lamp", null, null, null, null, null);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(item1, item2);
        verify(itemRepository).findAll(Mockito.<Specification<Item>>any());
    }

    @Test
    public void testGetItemById() {
        // Arrange
        Item item = new Item("Lamp", "Desk Lamp", 20.0, new Category("Lighting"), 4.5, "Lisbon");
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        Item result = itemService.getItemById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Lamp");
        verify(itemRepository).findById(1L);
    }

    @Test
    public void testGetItemById_NotFound() {
        // Arrange
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Item result = itemService.getItemById(99L);

        // Assert
        assertThat(result).isNull();
        verify(itemRepository).findById(99L);
    }
}
