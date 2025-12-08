package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
public class MockAddingItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private MockAddingItemService mockAddingItemService;

    @Test
    public void testInit_EmptyDb() {
        // Arrange
        when(itemRepository.count()).thenReturn(0L);

        // Act
        mockAddingItemService.init();

        // Assert
        verify(categoryRepository).saveAll(Mockito.<Iterable<tqs.backend.tqsbackend.entity.Category>>any());
        verify(itemRepository, times(10)).save(any(Item.class));
    }

    @Test
    public void testInit_NotEmptyDb() {
        // Arrange
        when(itemRepository.count()).thenReturn(5L);

        // Act
        mockAddingItemService.init();

        // Assert
        verify(categoryRepository, never()).saveAll(Mockito.<Iterable<tqs.backend.tqsbackend.entity.Category>>any());
        verify(itemRepository, never()).save(any(Item.class));
    }
}
