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

    @Test
    public void testFindByOwnerId_ReturnsItems() {
        // Arrange
        Category category = new Category("Electronics");
        Item item1 = new Item("Item 1", "Description 1", 10.0, category, 4.5, "Location 1", 1L);
        item1.setId(1L);
        Item item2 = new Item("Item 2", "Description 2", 20.0, category, 3.5, "Location 2", 1L);
        item2.setId(2L);
        List<Item> items = Arrays.asList(item1, item2);
        when(itemRepository.findByOwnerId(1L)).thenReturn(items);

        // Act
        List<Item> result = itemService.findByOwnerId(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(item1, item2);
        verify(itemRepository).findByOwnerId(1L);
    }

    @Test
    public void testCreateItem_Success() {
        // Arrange
        Category category = new Category("Electronics");
        Item newItem = new Item("New Item", "Description", 15.0, category, null, "Location", 1L);
        Item savedItem = new Item("New Item", "Description", 15.0, category, null, "Location", 1L);
        savedItem.setId(3L);
        when(itemRepository.save(newItem)).thenReturn(savedItem);

        // Act
        Item result = itemService.createItem(newItem);

        // Assert
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("New Item");
        verify(itemRepository).save(newItem);
    }

    @Test
    public void testCreateItem_NullOwnerId_ThrowsException() {
        // Arrange
        Category category = new Category("Electronics");
        Item newItem = new Item("New Item", "Description", 15.0, category, null, "Location");
        newItem.setOwnerId(null);

        // Act & Assert
        try {
            itemService.createItem(newItem);
            org.junit.jupiter.api.Assertions.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Owner ID cannot be null");
        }
    }

    @Test
    public void testActivateItem_Success() {
        // Arrange
        Category category = new Category("Electronics");
        Item item = new Item("Item 1", "Description", 10.0, category, 4.5, "Location", 1L);
        item.setId(2L);
        item.setActive(false);
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(itemRepository.save(Mockito.any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Item result = itemService.activateItem(2L, 1L);

        // Assert
        assertThat(result.isActive()).isTrue();
        verify(itemRepository).findById(2L);
        verify(itemRepository).save(item);
    }

    @Test
    public void testActivateItem_NotOwner_ThrowsException() {
        // Arrange
        Category category = new Category("Electronics");
        Item item = new Item("Item 1", "Description", 10.0, category, 4.5, "Location", 1L);
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act & Assert
        try {
            itemService.activateItem(1L, 2L);
            org.junit.jupiter.api.Assertions.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("User 2 is not the owner of item 1");
        }
    }

    @Test
    public void testDeactivateItem_Success() {
        // Arrange
        Category category = new Category("Electronics");
        Item item = new Item("Item 1", "Description", 10.0, category, 4.5, "Location", 1L);
        item.setId(1L);
        item.setActive(true);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(Mockito.any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Item result = itemService.deactivateItem(1L, 1L);

        // Assert
        assertThat(result.isActive()).isFalse();
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(item);
    }
}
