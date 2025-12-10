package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MockAddingItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MockAddingItemService mockAddingItemService;

    @Test
    public void testInit_EmptyDb() {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(itemRepository.count()).thenReturn(0L);

        // Act
        mockAddingItemService.init();

        // Assert
        verify(userRepository, times(1)).saveAll(anyList());
        verify(categoryRepository, times(1)).saveAll(anyList());
        verify(itemRepository, times(5)).save(any(Item.class));
    }

    @Test
    public void testInit_NotEmptyDb() {
        // Arrange - both counts > 0 means data exists
        when(userRepository.count()).thenReturn(3L);
        lenient().when(itemRepository.count()).thenReturn(5L);

        // Act
        mockAddingItemService.init();

        // Assert
        verify(userRepository, never()).saveAll(anyList());
        verify(categoryRepository, never()).saveAll(anyList());
        verify(itemRepository, never()).save(any(Item.class));
    }
}
