package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private tqs.backend.tqsbackend.repository.ItemRepository itemRepository;

        @Autowired
        private tqs.backend.tqsbackend.repository.CategoryRepository categoryRepository;

        @Autowired
        private tqs.backend.tqsbackend.repository.UserRepository userRepository;

        @Autowired
        private tqs.backend.tqsbackend.repository.BookingRepository bookingRepository;

        @Autowired
        private tqs.backend.tqsbackend.repository.RatingRepository ratingRepository;

        @org.junit.jupiter.api.BeforeEach
        public void setUp() {
                bookingRepository.deleteAll();
                ratingRepository.deleteAll();
                itemRepository.deleteAll();
                categoryRepository.deleteAll();
                userRepository.deleteAll();

                tqs.backend.tqsbackend.entity.User owner = new tqs.backend.tqsbackend.entity.User();
                owner.setName("Owner Name");
                owner.setEmail("owner@example.com");
                owner.setPassword("password");
                owner.setRole(tqs.backend.tqsbackend.entity.UserRoles.OWNER);
                userRepository.save(owner);

                tqs.backend.tqsbackend.entity.Category category = new tqs.backend.tqsbackend.entity.Category();
                category.setName("Lighting");
                categoryRepository.save(category);

                tqs.backend.tqsbackend.entity.Item item = new tqs.backend.tqsbackend.entity.Item();
                item.setName("Party Lights");
                item.setPrice(100.0);
                item.setCategory(category);
                item.setOwnerId(owner.getId());
                item.setAverageRating(4.5);
                item.setLocation("Lisbon");
                itemRepository.save(item);
        }

        @Test
        public void testSearchByKeyword() throws Exception {
                mockMvc.perform(get("/items/search").param("q", "Party"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("search"))
                                .andExpect(model().attribute("items",
                                                hasItem(hasProperty("name", containsString("Party")))));
        }

        @Test
        public void testFilterByCategory() throws Exception {
                mockMvc.perform(get("/items/search").param("category", "Lighting"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("items",
                                                everyItem(hasProperty("category",
                                                                hasProperty("name", is("Lighting"))))));
        }

        @Test
        public void testFilterByPriceRange() throws Exception {
                mockMvc.perform(get("/items/search").param("minPrice", "50").param("maxPrice", "200"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("items",
                                                everyItem(hasProperty("price", greaterThanOrEqualTo(50.0)))))
                                .andExpect(model().attribute("items",
                                                everyItem(hasProperty("price", lessThanOrEqualTo(200.0)))));
        }

        @Test
        public void testFilterByRating() throws Exception {
                mockMvc.perform(get("/items/search").param("minRating", "0.0"))
                                .andExpect(status().isOk())
                                .andExpect(
                                                model().attribute("items", everyItem(hasProperty("averageRating",
                                                                greaterThanOrEqualTo(0.0)))));
        }

        @Test
        public void testFilterByLocation() throws Exception {
                mockMvc.perform(get("/items/search").param("location", "Lisbon"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("items",
                                                everyItem(hasProperty("location", is("Lisbon")))));
        }

        @Test
        public void testCombinedFilter() throws Exception {
                mockMvc.perform(get("/items/search")
                                .param("q", "Party")
                                .param("category", "Lighting")
                                .param("minRating", "0.0"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("items",
                                                hasItem(hasProperty("name", containsString("Lights")))))
                                .andExpect(model().attribute("items",
                                                everyItem(hasProperty("category",
                                                                hasProperty("name", is("Lighting"))))))
                                .andExpect(
                                                model().attribute("items", everyItem(hasProperty("averageRating",
                                                                greaterThanOrEqualTo(0.0)))));
        }

        @Test
        public void testNoResults() throws Exception {
                mockMvc.perform(get("/items/search").param("q", "NonExistentTerm"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("items", empty()));
        }

        @Test
        public void testGetItemDetails() throws Exception {
                // Get the item created in setUp
                tqs.backend.tqsbackend.entity.Item item = itemRepository.findAll().get(0);

                mockMvc.perform(get("/items/" + item.getId()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("item_details"))
                                .andExpect(model().attribute("item", hasProperty("name", not(emptyString()))));
        }
}
