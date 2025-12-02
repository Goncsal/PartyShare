package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.ItemRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FavoriteControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ItemRepository itemRepository;

        @Test
        public void testAddFavoriteWithReferer() throws Exception {
                Item item = itemRepository.findAll().get(0);
                Long userId = 1L;
                String referer = "http://localhost:8080/custom-referer";

                mockMvc.perform(post("/favorites/" + userId + "/" + item.getId())
                                .header("Referer", referer))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl(referer))
                                .andExpect(flash().attribute("successMessage", "Item added to favorites!"));
        }

        @Test
        public void testAddFavoriteWithoutReferer() throws Exception {
                Item item = itemRepository.findAll().get(0);
                Long userId = 1L;

                mockMvc.perform(post("/favorites/" + userId + "/" + item.getId()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/search"))
                                .andExpect(flash().attribute("successMessage", "Item added to favorites!"));
        }

        @Test
        public void testRemoveFavoriteWithReferer() throws Exception {
                Item item = itemRepository.findAll().get(0);
                Long userId = 1L;

                // Add favorite first
                mockMvc.perform(post("/favorites/" + userId + "/" + item.getId()));

                String referer = "http://localhost:8080/custom-referer";

                // Remove favorite
                mockMvc.perform(post("/favorites/" + userId + "/" + item.getId() + "/remove")
                                .header("Referer", referer))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl(referer))
                                .andExpect(flash().attribute("successMessage", "Item removed from favorites!"));
        }

        @Test
        public void testRemoveFavoriteWithoutReferer() throws Exception {
                Item item = itemRepository.findAll().get(0);
                Long userId = 1L;

                // Add favorite first
                mockMvc.perform(post("/favorites/" + userId + "/" + item.getId()));

                // Remove favorite
                mockMvc.perform(post("/favorites/" + userId + "/" + item.getId() + "/remove"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/favorites/" + userId))
                                .andExpect(flash().attribute("successMessage", "Item removed from favorites!"));
        }
}
