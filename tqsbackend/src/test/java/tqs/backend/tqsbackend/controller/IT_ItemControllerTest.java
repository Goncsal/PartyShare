package tqs.backend.tqsbackend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IT_ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void createItem_WithValidData_RedirectsToMyItems() throws Exception {
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPassword("password");
        owner.setName("Owner");
        owner.setRole(UserRoles.OWNER);
        userRepository.save(owner);

        Category category = categoryRepository.findByName("Electronics");
        if (category == null) {
            category = new Category();
            category.setName("Electronics");
            category = categoryRepository.save(category);
        }

        mockMvc.perform(post("/items")
                .sessionAttr("userId", owner.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "New Item")
                .param("description", "Description")
                .param("price", "10.0")
                .param("categoryId", category.getId().toString())
                .param("location", "Aveiro"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items/*"));

        List<Item> items = itemRepository.findByOwnerId(owner.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("New Item");
    }

    @Test
    void updateItem_WithValidData_UpdatesItem() throws Exception {
        User owner = new User();
        owner.setEmail("owner2@example.com");
        owner.setPassword("password");
        owner.setName("Owner2");
        owner.setRole(UserRoles.OWNER);
        userRepository.save(owner);

        Category category = categoryRepository.findByName("Tools");
        if (category == null) {
            category = new Category();
            category.setName("Tools");
            category = categoryRepository.save(category);
        }

        Item item = new Item();
        item.setName("Old Name");
        item.setDescription("Old Desc");
        item.setPrice(5.0);
        item.setCategory(category);
        item.setOwnerId(owner.getId());
        item.setLocation("Porto");
        itemRepository.save(item);

        mockMvc.perform(post("/items/" + item.getId() + "/edit")
                .sessionAttr("userId", owner.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Updated Name")
                .param("description", "Updated Desc")
                .param("price", "15.0")
                .param("categoryId", category.getId().toString())
                .param("location", "Lisbon"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/my-items"));

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(updatedItem.getName()).isEqualTo("Updated Name");
        assertThat(updatedItem.getPrice()).isEqualTo(15.0);
    }
}
