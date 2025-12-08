package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void testGetAllCategories() {
        // Arrange
        Category cat1 = new Category("Electronics");
        Category cat2 = new Category("Lighting");
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(cat1, cat2));

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(cat1, cat2);
        verify(categoryRepository).findAll();
    }

    @Test
    public void testGetCategoryByName() {
        // Arrange
        Category cat = new Category("Electronics");
        when(categoryRepository.findByName("Electronics")).thenReturn(cat);

        // Act
        Category result = categoryService.getCategoryByName("Electronics");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).findByName("Electronics");
    }
}
