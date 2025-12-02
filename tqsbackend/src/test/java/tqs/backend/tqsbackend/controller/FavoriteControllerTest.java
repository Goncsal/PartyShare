package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.FavoriteService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @Test
    void getFavorites_LoggedIn() throws Exception {
        Item item = new Item();
        item.setName("Test Item");
        List<Item> favorites = Arrays.asList(item);

        when(favoriteService.getFavorites(1L)).thenReturn(favorites);

        mvc.perform(get("/favorites").sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("favorites"))
                .andExpect(model().attribute("favorites", favorites))
                .andExpect(model().attribute("userId", 1L));
    }

    @Test
    void getFavorites_LoggedOut() throws Exception {
        mvc.perform(get("/favorites"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void addFavorite_LoggedIn() throws Exception {
        mvc.perform(post("/favorites/10")
                .sessionAttr("userId", 1L)
                .header("Referer", "/items/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"))
                .andExpect(flash().attribute("successMessage", "Item added to favorites!"));

        verify(favoriteService).addFavorite(1L, 10L);
    }

    @Test
    void addFavorite_LoggedOut() throws Exception {
        mvc.perform(post("/favorites/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void removeFavorite_LoggedIn() throws Exception {
        mvc.perform(post("/favorites/10/remove")
                .sessionAttr("userId", 1L)
                .header("Referer", "/favorites"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/favorites"))
                .andExpect(flash().attribute("successMessage", "Item removed from favorites!"));

        verify(favoriteService).removeFavorite(1L, 10L);
    }

    @Test
    void removeFavorite_LoggedOut() throws Exception {
        mvc.perform(post("/favorites/10/remove"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }
}
