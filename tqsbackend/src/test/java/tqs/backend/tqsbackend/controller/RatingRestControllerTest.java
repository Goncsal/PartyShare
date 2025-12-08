package tqs.backend.tqsbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Rating;
import tqs.backend.tqsbackend.entity.RatingType;
import tqs.backend.tqsbackend.fixtures.RatingTestFixtures;
import tqs.backend.tqsbackend.service.RatingService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatingRestController.class)
class RatingRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RatingService ratingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRating_ReturnsCreated() throws Exception {
        Rating rating = RatingTestFixtures.sampleRating(10L, 1L, 2L);

        when(ratingService.createRating(anyLong(), any(RatingType.class), anyLong(), anyInt(), anyString()))
                .thenReturn(rating);

        mvc.perform(post("/api/ratings/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rating)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.rate", is(5)));
    }

    @Test
    void getRatingById_Found_ReturnsRating() throws Exception {
        Rating rating = RatingTestFixtures.sampleRating(10L, 1L, 2L);

        when(ratingService.getRatingById(10L)).thenReturn(Optional.of(rating));

        mvc.perform(get("/api/ratings/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    void getRatingById_NotFound_Returns404() throws Exception {
        when(ratingService.getRatingById(10L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/ratings/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRating_Success_ReturnsNoContent() throws Exception {
        when(ratingService.deleteRating(10L)).thenReturn(true);

        mvc.perform(delete("/api/ratings/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRating_NotFound_ReturnsNotFound() throws Exception {
        when(ratingService.deleteRating(10L)).thenReturn(false);

        mvc.perform(delete("/api/ratings/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchRatings_ReturnsList() throws Exception {
        Rating r1 = RatingTestFixtures.sampleRating(1L, 1L, 2L);
        Rating r2 = RatingTestFixtures.sampleRating(2L, 1L, 3L);
        List<Rating> ratings = Arrays.asList(r1, r2);

        when(ratingService.getAllRatings()).thenReturn(ratings);

        mvc.perform(get("/api/ratings/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
    
    @Test
    void searchRatings_BySender_ReturnsList() throws Exception {
        Rating r1 = RatingTestFixtures.sampleRating(1L, 1L, 2L);
        List<Rating> ratings = Arrays.asList(r1);

        when(ratingService.getRatingBySenderId(1L)).thenReturn(ratings);

        mvc.perform(get("/api/ratings/search").param("sender", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
