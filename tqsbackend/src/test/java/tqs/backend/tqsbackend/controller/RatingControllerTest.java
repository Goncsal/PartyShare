package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.service.RatingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatingController.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RatingService ratingService;

    @Test
    void createRating_Success() throws Exception {
        mvc.perform(post("/ratings/new")
                .param("senderId", "1")
                .param("ratingType", "OWNER")
                .param("ratedId", "2")
                .param("rate", "5")
                .param("comment", "Great service!")
                .header("Referer", "/items/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/1"))
                .andExpect(flash().attribute("successMessage", "Rating submitted successfully!"));

        verify(ratingService).createRating(1L, RatingType.OWNER, 2L, 5, "Great service!");
    }

    @Test
    void createRating_Failure() throws Exception {
        doThrow(new IllegalArgumentException("Invalid rating")).when(ratingService)
                .createRating(anyLong(), any(RatingType.class), anyLong(), anyInt(), anyString());

        mvc.perform(post("/ratings/new")
                .param("senderId", "1")
                .param("ratingType", "OWNER")
                .param("ratedId", "2")
                .param("rate", "6") // Invalid rate
                .param("comment", "Bad")
                .header("Referer", "/items/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/1"))
                .andExpect(flash().attribute("errorMessage", "Invalid rating"));
    }

    @Test
    void createRating_NoReferer_RedirectsToSearch() throws Exception {
        mvc.perform(post("/ratings/new")
                .param("senderId", "1")
                .param("ratingType", "OWNER")
                .param("ratedId", "2")
                .param("rate", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"));
    }
}
