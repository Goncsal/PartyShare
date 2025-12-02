package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.ItemRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FavoriteRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testAddAndGetFavorites() throws Exception {
        Item item = itemRepository.findAll().get(0);
        Long userId = 1L;

        // Add favorite
        mockMvc.perform(post("/api/favorites/" + userId + "/" + item.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Get favorites
        mockMvc.perform(get("/api/favorites/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id", is(item.getId().intValue())));
    }

    @Test
    public void testRemoveFavorite() throws Exception {
        Item item = itemRepository.findAll().get(0);
        Long userId = 1L;

        // Add favorite first
        mockMvc.perform(post("/api/favorites/" + userId + "/" + item.getId()));

        // Remove favorite
        mockMvc.perform(delete("/api/favorites/" + userId + "/" + item.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify it's gone
        mockMvc.perform(get("/api/favorites/" + userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(hasItem(hasProperty("id", is(item.getId()))))));
    }
}
