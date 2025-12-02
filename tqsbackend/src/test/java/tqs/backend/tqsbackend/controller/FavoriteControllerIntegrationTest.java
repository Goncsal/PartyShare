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
    public void testAddAndGetFavorites() throws Exception {
        Item item = itemRepository.findAll().get(0);
        Long userId = 1L;

        // Add favorite
        mockMvc.perform(post("/favorites/" + userId + "/" + item.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"));

        // Get favorites
        mockMvc.perform(get("/favorites/" + userId))
                .andExpect(status().isOk())
                .andExpect(view().name("favorites"))
                .andExpect(model().attribute("favorites", hasItem(hasProperty("id", is(item.getId())))));
    }

    @Test
    public void testRemoveFavorite() throws Exception {
        Item item = itemRepository.findAll().get(0);
        Long userId = 1L;

        // Add favorite first
        mockMvc.perform(post("/favorites/" + userId + "/" + item.getId()));

        // Remove favorite
        mockMvc.perform(post("/favorites/" + userId + "/" + item.getId() + "/remove"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favorites/" + userId));

        // Verify it's gone
        mockMvc.perform(get("/favorites/" + userId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("favorites", not(hasItem(hasProperty("id", is(item.getId()))))));
    }
}
