package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerDashboardController.class)
@Import(OwnerDashboardControllerTest.TestConfig.class)
public class OwnerDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private MockHttpSession session;
    private User ownerUser;
    private User renterUser;
    private List<Item> items;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ItemService itemService() {
            return mock(ItemService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();

        ownerUser = new User("Owner", "owner@test.com", "password", UserRoles.OWNER);
        ownerUser.setId(1L);

        renterUser = new User("Renter", "renter@test.com", "password", UserRoles.RENTER);
        renterUser.setId(2L);

        Category category = new Category("Electronics");
        Item item1 = new Item("Item 1", "Desc 1", 10.0, category, 4.5, "Location 1", 1L);
        item1.setId(1L);
        Item item2 = new Item("Item 2", "Desc 2", 20.0, category, 3.5, "Location 2", 1L);
        item2.setId(2L);

        items = Arrays.asList(item1, item2);
    }

    @Test
    void testShowDashboard_Success() throws Exception {
        session.setAttribute("userId", 1L);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.findByOwnerId(1L)).willReturn(items);

        mockMvc.perform(get("/owner/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/owner"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attribute("isLoggedIn", true))
                .andExpect(model().attribute("userName", "Owner"));
    }

    @Test
    void testShowDashboard_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/owner/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void testShowDashboard_NotOwner() throws Exception {
        session.setAttribute("userId", 2L);

        given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

        mockMvc.perform(get("/owner/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("error", "Access denied. Owner role required."));
    }

    @Test
    void testShowDashboard_UserNotFound() throws Exception {
        session.setAttribute("userId", 99L);

        given(userService.getUserById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/owner/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("error", "Access denied. Owner role required."));
    }

    @Test
    void testShowDashboard_EmptyItemsList() throws Exception {
        session.setAttribute("userId", 1L);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
        given(itemService.findByOwnerId(1L)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/owner/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/owner"))
                .andExpect(model().attribute("items", Collections.emptyList()));
    }

    @Test
    void testShowAddItemPage_Success() throws Exception {
        session.setAttribute("userId", 1L);

        given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));

        mockMvc.perform(get("/owner/dashboard/add-item").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/add_item"));
    }

    @Test
    void testShowAddItemPage_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/owner/dashboard/add-item"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void testShowAddItemPage_NotOwner() throws Exception {
        session.setAttribute("userId", 2L);

        given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

        mockMvc.perform(get("/owner/dashboard/add-item").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("error", "Access denied. Owner role required."));
    }
}
