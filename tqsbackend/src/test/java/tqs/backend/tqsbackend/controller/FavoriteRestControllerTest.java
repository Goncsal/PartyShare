package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.FavoriteService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteRestController.class)
class FavoriteRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @Test
    void getFavorites_ReturnsUserFavorites() throws Exception {
        Item item = createTestItem();
        when(favoriteService.getFavorites(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/favorites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void getFavorites_EmptyList_ReturnsEmpty() throws Exception {
        when(favoriteService.getFavorites(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/favorites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addFavorite_Success() throws Exception {
        doNothing().when(favoriteService).addFavorite(1L, 2L);

        mockMvc.perform(post("/api/favorites/1/2"))
                .andExpect(status().isOk());

        verify(favoriteService).addFavorite(1L, 2L);
    }

    @Test
    void removeFavorite_Success() throws Exception {
        doNothing().when(favoriteService).removeFavorite(1L, 2L);

        mockMvc.perform(delete("/api/favorites/1/2"))
                .andExpect(status().isOk());

        verify(favoriteService).removeFavorite(1L, 2L);
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
