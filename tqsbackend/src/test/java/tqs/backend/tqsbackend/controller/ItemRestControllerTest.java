package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRestController.class)
class ItemRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void searchItems_WithNoParams_ReturnsAllItems() throws Exception {
        Item item = createTestItem();
        when(itemService.searchItems(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_WithQuery_ReturnsFilteredItems() throws Exception {
        Item item = createTestItem();
        when(itemService.searchItems(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/search")
                        .param("q", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_WithCategory_ReturnsFilteredItems() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Item item = createTestItem();
        item.setCategory(category);

        when(categoryService.getCategoryByName("Electronics")).thenReturn(category);
        when(itemService.searchItems(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/search")
                        .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_WithPriceRange_ReturnsFilteredItems() throws Exception {
        Item item = createTestItem();
        when(itemService.searchItems(any(), any(), anyDouble(), anyDouble(), any(), any()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/search")
                        .param("minPrice", "10.0")
                        .param("maxPrice", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_WithLocation_ReturnsFilteredItems() throws Exception {
        Item item = createTestItem();
        when(itemService.searchItems(any(), any(), any(), any(), any(), anyString()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/search")
                        .param("location", "Porto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItems_WithAllParams_ReturnsFilteredItems() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Item item = createTestItem();
        item.setCategory(category);

        when(categoryService.getCategoryByName("Electronics")).thenReturn(category);
        when(itemService.searchItems(anyString(), any(), anyDouble(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/search")
                        .param("q", "Test")
                        .param("category", "Electronics")
                        .param("minPrice", "10.0")
                        .param("maxPrice", "100.0")
                        .param("minRating", "4.0")
                        .param("location", "Porto"))
                .andExpect(status().isOk());
    }

    private Item createTestItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test description");
        item.setPrice(50.0);
        item.setLocation("Porto");
        item.setOwnerId(1L);
        item.setActive(true);
        return item;
    }
}
