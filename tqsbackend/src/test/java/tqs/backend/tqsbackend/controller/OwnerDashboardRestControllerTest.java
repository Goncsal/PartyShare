package tqs.backend.tqsbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.RatingService;
import tqs.backend.tqsbackend.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerDashboardRestController.class)
@Import(OwnerDashboardRestControllerTest.TestConfig.class)
public class OwnerDashboardRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;
    private User ownerUser;
    private User renterUser;
    private Category category;
    private Item item1;
    private Item item2;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();

        category = new Category("Electronics");
        category.setId(1L);

        ownerUser = new User("Owner User", "owner@example.com", "password", UserRoles.OWNER);
        ownerUser.setId(1L);

        renterUser = new User("Renter User", "renter@example.com", "password", UserRoles.RENTER);
        renterUser.setId(2L);

        item1 = new Item("Item 1", "Description 1", 10.0, category, 4.5, "Location 1", 1L);
        item1.setId(1L);
        item1.setActive(true);

        item2 = new Item("Item 2", "Description 2", 20.0, category, 3.5, "Location 2", 1L);
        item2.setId(2L);
        item2.setActive(false);
    }

    @Test
    void testGetOwnerItems_Success() throws Exception {
        session.setAttribute("userId", 1L);
        List<Item> items = Arrays.asList(item1, item2);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.findByOwnerId(1L)).willReturn(items);

        mockMvc.perform(get("/api/owner/dashboard/items")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[1].name").value("Item 2"));

        verify(itemService, times(1)).findByOwnerId(1L);
    }

    @Test
    void testGetOwnerItems_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/owner/dashboard/items"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));

        verify(itemService, never()).findByOwnerId(anyLong());
    }

    @Test
    void testGetOwnerItems_NotOwner() throws Exception {
        session.setAttribute("userId", 2L);
        given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

        mockMvc.perform(get("/api/owner/dashboard/items")
                .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("User is not an owner"));

        verify(itemService, never()).findByOwnerId(anyLong());
    }

    @Test
    void testCreateItem_Success() throws Exception {
        session.setAttribute("userId", 1L);
        Item newItem = new Item("New Item", "New Description", 15.0, category, null, "New Location", 1L);
        newItem.setId(3L);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.createItem(any(Item.class))).willReturn(newItem);

        mockMvc.perform(post("/api/owner/dashboard/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Item"));

        verify(itemService, times(1)).createItem(any(Item.class));
    }

    @Test
    void testCreateItem_NotOwner() throws Exception {
        session.setAttribute("userId", 2L);
        Item newItem = new Item("New Item", "New Description", 15.0, category, null, "New Location", 2L);

        given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

        mockMvc.perform(post("/api/owner/dashboard/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("User is not an owner"));

        verify(itemService, never()).createItem(any(Item.class));
    }

    @Test
    void testActivateItem_Success() throws Exception {
        session.setAttribute("userId", 1L);
        item2.setActive(true);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.activateItem(2L, 1L)).willReturn(item2);

        mockMvc.perform(patch("/api/owner/dashboard/items/2/activate")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(itemService, times(1)).activateItem(2L, 1L);
    }

    @Test
    void testActivateItem_NotOwnerOfItem() throws Exception {
        session.setAttribute("userId", 1L);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.activateItem(2L, 1L))
                .willThrow(new IllegalArgumentException("User 1 is not the owner of item 2"));

        mockMvc.perform(patch("/api/owner/dashboard/items/2/activate")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User 1 is not the owner of item 2"));
    }

    @Test
    void testDeactivateItem_Success() throws Exception {
        session.setAttribute("userId", 1L);
        item1.setActive(false);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.deactivateItem(1L, 1L)).willReturn(item1);

        mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(itemService, times(1)).deactivateItem(1L, 1L);
    }

    @Test
    void testGetItemRatings_Success() throws Exception {
        session.setAttribute("userId", 1L);
        Rating rating1 = new Rating(2L, RatingType.PRODUCT, 1L, 5, "Great item!");
        rating1.setId(1L);
        Rating rating2 = new Rating(3L, RatingType.PRODUCT, 1L, 4, "Good item");
        rating2.setId(2L);
        List<Rating> ratings = Arrays.asList(rating1, rating2);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.getItemById(1L)).willReturn(item1);
        given(ratingService.getRatingByRatedInfo(RatingType.PRODUCT, 1L)).willReturn(ratings);

        mockMvc.perform(get("/api/owner/dashboard/items/1/ratings")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rate").value(5))
                .andExpect(jsonPath("$[0].comment").value("Great item!"))
                .andExpect(jsonPath("$[1].rate").value(4));

        verify(ratingService, times(1)).getRatingByRatedInfo(RatingType.PRODUCT, 1L);
    }

    @Test
    void testGetItemRatings_NotOwnerOfItem() throws Exception {
        session.setAttribute("userId", 2L);
        item1.setOwnerId(1L);

        given(userService.getUserById(2L)).willReturn(Optional.of(ownerUser));
        given(itemService.getItemById(1L)).willReturn(item1);

        mockMvc.perform(get("/api/owner/dashboard/items/1/ratings")
                .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Item does not belong to this owner"));

        verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
    }

    @Test
    void testCreateItem_NotLoggedIn() throws Exception {
        Item newItem = new Item("New Item", "New Description", 15.0, category, null, "New Location", 1L);

        mockMvc.perform(post("/api/owner/dashboard/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));

        verify(itemService, never()).createItem(any(Item.class));
    }

    @Test
    void testCreateItem_ValidationFailure() throws Exception {
        session.setAttribute("userId", 1L);
        Item newItem = new Item("New Item", "New Description", 15.0, category, null, "New Location", 1L);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.createItem(any(Item.class)))
                .willThrow(new IllegalArgumentException("Owner ID cannot be null"));

        mockMvc.perform(post("/api/owner/dashboard/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Owner ID cannot be null"));
    }

    @Test
    void testActivateItem_NotLoggedIn() throws Exception {
        mockMvc.perform(patch("/api/owner/dashboard/items/1/activate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));

        verify(itemService, never()).activateItem(anyLong(), anyLong());
    }

    @Test
    void testActivateItem_NotOwnerRole() throws Exception {
        session.setAttribute("userId", 2L);
        given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

        mockMvc.perform(patch("/api/owner/dashboard/items/1/activate")
                .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("User is not an owner"));

        verify(itemService, never()).activateItem(anyLong(), anyLong());
    }

    @Test
    void testDeactivateItem_NotLoggedIn() throws Exception {
        mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));

        verify(itemService, never()).deactivateItem(anyLong(), anyLong());
    }

    @Test
    void testDeactivateItem_NotOwnerRole() throws Exception {
        session.setAttribute("userId", 2L);
        given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

        mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate")
                .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("User is not an owner"));

        verify(itemService, never()).deactivateItem(anyLong(), anyLong());
    }

    @Test
    void testDeactivateItem_NotOwnerOfItem() throws Exception {
        session.setAttribute("userId", 1L);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.deactivateItem(1L, 1L))
                .willThrow(new IllegalArgumentException("User 1 is not the owner of item 1"));

        mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User 1 is not the owner of item 1"));
    }

    @Test
    void testGetItemRatings_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/owner/dashboard/items/1/ratings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));

        verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
    }

    @Test
    void testGetItemRatings_NotOwnerRole() throws Exception {
        session.setAttribute("userId", 2L);
        given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

        mockMvc.perform(get("/api/owner/dashboard/items/1/ratings")
                .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("User is not an owner"));

        verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
    }

    @Test
    void testGetItemRatings_ItemNotFound() throws Exception {
        session.setAttribute("userId", 1L);
        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.getItemById(99L)).willReturn(null);

        mockMvc.perform(get("/api/owner/dashboard/items/99/ratings")
                .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));

        verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
    }
}
