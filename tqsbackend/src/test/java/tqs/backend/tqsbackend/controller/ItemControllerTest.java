package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.UserService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(ItemControllerTest.TestConfig.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemService itemService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ItemService itemService() {
            return Mockito.mock(ItemService.class);
        }

        @Bean
        public CategoryService categoryService() {
            return Mockito.mock(CategoryService.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Test
    public void testGetItemDetails() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        tqs.backend.tqsbackend.entity.Category category = new tqs.backend.tqsbackend.entity.Category();
        category.setName("Test Category");
        item.setCategory(category);

        given(itemService.getItemById(1L)).willReturn(item);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item_details"))
                .andExpect(model().attribute("item", item));
    }
}
