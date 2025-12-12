package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_NewName_CreatesCategory() {
        String name = "New Category";
        given(categoryRepository.findByName(name)).willReturn(null);
        given(categoryRepository.save(any(Category.class))).willAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        Category result = categoryService.createCategory(name);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        String name = "Existing Category";
        given(categoryRepository.findByName(name)).willReturn(new Category(name));

        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(name));
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
