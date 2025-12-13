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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;

@WebMvcTest(ItemController.class)

public class ItemControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ItemService itemService;

        @MockitoBean
        private CategoryService categoryService;

        @MockitoBean
        private tqs.backend.tqsbackend.service.UserService userService;

        @Test
        public void testGetItemDetails() throws Exception {
                Item item = new Item();
                item.setId(1L);
                item.setName("Test Item");
                item.setOwnerId(2L); // Set owner ID
                tqs.backend.tqsbackend.entity.Category category = new tqs.backend.tqsbackend.entity.Category();
                category.setName("Test Category");
                item.setCategory(category);

                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setId(2L);
                owner.setName("Owner Name");
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(itemService.getItemById(1L)).willReturn(item);
                given(userService.getUserById(2L)).willReturn(java.util.Optional.of(owner)); // Mock owner retrieval

                mockMvc.perform(get("/items/1"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("item_details"))
                                .andExpect(model().attribute("item", item))
                                .andExpect(model().attribute("owner", owner)); // Expect owner in model
        }

        @Test
        public void testGetItemDetails_LoggedIn() throws Exception {
                Item item = new Item();
                item.setId(1L);
                item.setName("Test Item");
                item.setOwnerId(2L); // Set owner ID
                tqs.backend.tqsbackend.entity.Category category = new tqs.backend.tqsbackend.entity.Category();
                category.setName("Test Category");
                item.setCategory(category);

                tqs.backend.tqsbackend.entity.User user = new tqs.backend.tqsbackend.entity.User();
                user.setId(1L);
                user.setRole(tqs.backend.tqsbackend.entity.UserRoles.RENTER);

                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setId(2L);
                owner.setName("Owner Name");
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(itemService.getItemById(1L)).willReturn(item);
                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(user));
                given(userService.getUserById(2L)).willReturn(java.util.Optional.of(owner)); // Mock owner retrieval

                mockMvc.perform(get("/items/1")
                                .sessionAttr("userId", 1L))
                                .andExpect(status().isOk())
                                .andExpect(view().name("item_details"))
                                .andExpect(model().attribute("item", item))
                                .andExpect(model().attribute("userRole", "RENTER"))
                                .andExpect(model().attribute("owner", owner)); // Expect owner in model
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
        void searchItems_LoggedIn_ReturnsViewAndModelWithUserRole() throws Exception {
                given(categoryService.getAllCategories()).willReturn(Collections.emptyList());
                given(itemService.searchItems(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                Mockito.any()))
                                .willReturn(Collections.emptyList());

                tqs.backend.tqsbackend.entity.User user = new tqs.backend.tqsbackend.entity.User();
                user.setRole(tqs.backend.tqsbackend.entity.UserRoles.RENTER);
                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(user));

                mockMvc.perform(get("/items/search")
                                .sessionAttr("userId", 1L))
                                .andExpect(status().isOk())
                                .andExpect(view().name("search"))
                                .andExpect(model().attributeExists("items", "categories"))
                                .andExpect(model().attribute("userRole", "RENTER"));
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

        @Test
        void showEditItemForm_Owner_ReturnsView() throws Exception {
                Item item = new Item();
                item.setId(1L);
                item.setOwnerId(1L);
                tqs.backend.tqsbackend.entity.Category category = new tqs.backend.tqsbackend.entity.Category();
                category.setName("Test Category");
                item.setCategory(category);

                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));
                given(itemService.getItemById(1L)).willReturn(item);
                given(categoryService.getAllCategories()).willReturn(Collections.emptyList());

                mockMvc.perform(get("/items/1/edit")
                                .sessionAttr("userId", 1L))
                                .andExpect(status().isOk())
                                .andExpect(view().name("items/edit_item"))
                                .andExpect(model().attributeExists("item", "categories"));
        }

        @Test
        void showEditItemForm_NonOwner_Redirects() throws Exception {
                Item item = new Item();
                item.setId(1L);
                item.setOwnerId(2L); // Different owner

                tqs.backend.tqsbackend.entity.User user = new tqs.backend.tqsbackend.entity.User();
                user.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(user));
                given(itemService.getItemById(1L)).willReturn(item);

                mockMvc.perform(get("/items/1/edit")
                                .sessionAttr("userId", 1L))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/search"));
        }

        @Test
        void showEditItemForm_ItemNotFound_Redirects() throws Exception {
                given(itemService.getItemById(1L)).willReturn(null);

                mockMvc.perform(get("/items/1/edit")
                                .sessionAttr("userId", 1L))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/search"));
        }

        @Test
        void showEditItemForm_ItemNotFound_LoggedIn_Redirects() throws Exception {
                given(itemService.getItemById(1L)).willReturn(null);

                mockMvc.perform(get("/items/1/edit")
                                .sessionAttr("userId", 1L))
                                .andExpect(status().is3xxRedirection());
        }

        @Test
        void updateItem_Owner_Success() throws Exception {
                Item item = new Item();
                item.setId(1L);

                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));
                given(itemService.updateItem(Mockito.eq(1L), Mockito.any(Item.class), Mockito.eq(1L))).willReturn(item);

                mockMvc.perform(post("/items/1/edit")
                                .sessionAttr("userId", 1L)
                                .param("name", "Updated Name")
                                .param("price", "20.0")
                                .param("categoryId", "1"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/my-items"));
        }

        @Test
        void updateItem_NonOwner_Forbidden() throws Exception {
                tqs.backend.tqsbackend.entity.User user = new tqs.backend.tqsbackend.entity.User();
                user.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(user));
                given(itemService.updateItem(Mockito.eq(1L), Mockito.any(Item.class), Mockito.eq(1L)))
                                .willThrow(new IllegalArgumentException("Not owner"));

                mockMvc.perform(post("/items/1/edit")
                                .sessionAttr("userId", 1L)
                                .param("name", "Updated Name")
                                .param("categoryId", "1"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/search"));
        }

        @Test
        void toggleItemStatus_Owner_Activate_Success() throws Exception {
                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));

                mockMvc.perform(post("/items/1/toggle-status")
                                .sessionAttr("userId", 1L)
                                .param("active", "true"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/my-items"));

                verify(itemService).activateItem(1L, 1L);
        }

        @Test
        void toggleItemStatus_Owner_Deactivate_Success() throws Exception {
                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));

                mockMvc.perform(post("/items/1/toggle-status")
                                .sessionAttr("userId", 1L)
                                .param("active", "false"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/my-items"));

                verify(itemService).deactivateItem(1L, 1L);
        }

        @Test
        void toggleItemStatus_Failure_RedirectsToSearch() throws Exception {
                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);

                given(userService.getUserById(1L)).willReturn(java.util.Optional.of(owner));
                given(itemService.activateItem(1L, 1L)).willThrow(new IllegalArgumentException("Error"));

                mockMvc.perform(post("/items/1/toggle-status")
                                .sessionAttr("userId", 1L)
                                .param("active", "true"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/items/search"));
        }
}
