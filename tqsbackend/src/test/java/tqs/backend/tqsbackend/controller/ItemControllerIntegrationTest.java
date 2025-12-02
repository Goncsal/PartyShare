package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.repository.ItemRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testSearchByKeyword() throws Exception {
        mockMvc.perform(get("/items/search").param("q", "Lamp"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("items", hasItem(hasProperty("name", containsString("Lamp")))));
    }

    @Test
    public void testFilterByCategory() throws Exception {
        mockMvc.perform(get("/items/search").param("category", "Lighting"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items",
                        everyItem(hasProperty("category", hasProperty("name", is("Lighting"))))));
    }

    @Test
    public void testFilterByPriceRange() throws Exception {
        mockMvc.perform(get("/items/search").param("minPrice", "50").param("maxPrice", "200"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", everyItem(hasProperty("price", greaterThanOrEqualTo(50.0)))))
                .andExpect(model().attribute("items", everyItem(hasProperty("price", lessThanOrEqualTo(200.0)))));
    }

    @Test
    public void testFilterByRating() throws Exception {
        mockMvc.perform(get("/items/search").param("minRating", "4.0"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("items", everyItem(hasProperty("averageRating", greaterThanOrEqualTo(4.0)))));
    }

    @Test
    public void testFilterByLocation() throws Exception {
        mockMvc.perform(get("/items/search").param("location", "Lisbon"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", everyItem(hasProperty("location", is("Lisbon")))));
    }

    @Test
    public void testCombinedFilter() throws Exception {
        mockMvc.perform(get("/items/search")
                .param("q", "Lamp")
                .param("category", "Lighting")
                .param("minRating", "4.0"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", hasItem(hasProperty("name", containsString("Lamp")))))
                .andExpect(model().attribute("items",
                        everyItem(hasProperty("category", hasProperty("name", is("Lighting"))))))
                .andExpect(
                        model().attribute("items", everyItem(hasProperty("averageRating", greaterThanOrEqualTo(4.0)))));
    }

    @Test
    public void testNoResults() throws Exception {
        mockMvc.perform(get("/items/search").param("q", "NonExistentTerm"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", empty()));
    }
}
