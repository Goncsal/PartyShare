package tqs.backend.tqsbackend.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;

@WebMvcTest(ItemController.class)
@Import(ItemControllerTest.TestConfig.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private tqs.backend.tqsbackend.service.UserService userService;

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
        public tqs.backend.tqsbackend.service.UserService userService() {
            return Mockito.mock(tqs.backend.tqsbackend.service.UserService.class);
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
        given(userService.getUserById(Mockito.anyLong())).willReturn(java.util.Optional.empty());

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item_details"))
                .andExpect(model().attribute("item", item));
    }

    @Test
    void showNewItemForm_NotLoggedIn_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/items/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void showNewItemForm_LoggedIn_ReturnsForm() throws Exception {
        given(categoryService.getAllCategories()).willReturn(Collections.emptyList());

        tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
        owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);
        given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));

        mockMvc.perform(get("/items/new")
                .sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("items/new_item"))
                .andExpect(model().attributeExists("item", "categories"));
    }

    @Test
    void createItem_NotLoggedIn_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/items")
                .param("name", "Test")
                .param("price", "10.0")
                .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void createItem_LoggedIn_CreatesItemAndRedirects() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setName("New Item");

        tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
        owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);
        given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));

        given(itemService.saveItem(Mockito.any(Item.class))).willReturn(item);

        mockMvc.perform(post("/items")
                .sessionAttr("userId", 1L)
                .param("name", "New Item")
                .param("price", "10.0")
                .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/1"));

        verify(itemService).saveItem(Mockito.any(Item.class));
    }

    @Test
    void getMyItems_NotLoggedIn_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/items/my-items"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void getMyItems_LoggedIn_ReturnsItems() throws Exception {
        tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
        owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);
        given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));

        given(itemService.findByOwnerId(1L)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/items/my-items")
                .sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("items/my_items"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    void searchItems_ReturnsViewAndModel() throws Exception {
        given(categoryService.getAllCategories()).willReturn(Collections.emptyList());
        given(itemService.searchItems(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any()))
                .willReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("items", "categories"));
    }

    @Test
    void showNewItemForm_LoggedInButNotOwner_RedirectsToSearch() throws Exception {
        tqs.backend.tqsbackend.entity.User renter = new tqs.backend.tqsbackend.entity.User();
        renter.setRole(tqs.backend.tqsbackend.entity.UserRoles.RENTER);
        given(userService.getUserById(1L)).willReturn(java.util.Optional.of(renter));

        mockMvc.perform(get("/items/new")
                .sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"));
    }

    @Test
    void createItem_LoggedInButNotOwner_RedirectsToSearch() throws Exception {
        tqs.backend.tqsbackend.entity.User renter = new tqs.backend.tqsbackend.entity.User();
        renter.setRole(tqs.backend.tqsbackend.entity.UserRoles.RENTER);
        given(userService.getUserById(1L)).willReturn(java.util.Optional.of(renter));

        mockMvc.perform(post("/items")
                .sessionAttr("userId", 1L)
                .param("name", "Test")
                .param("price", "10.0")
                .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"));
    }

    @Test
    void getMyItems_LoggedInButNotOwner_RedirectsToSearch() throws Exception {
        tqs.backend.tqsbackend.entity.User renter = new tqs.backend.tqsbackend.entity.User();
        renter.setRole(tqs.backend.tqsbackend.entity.UserRoles.RENTER);
        given(userService.getUserById(1L)).willReturn(java.util.Optional.of(renter));

        mockMvc.perform(get("/items/my-items")
                .sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"));
    }
}
