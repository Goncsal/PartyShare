package tqs.backend.tqsbackend.controller.pict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.RatingRepository;
import tqs.backend.tqsbackend.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

/**
 * PICT-based Parameterized Tests for Item Search
 * 
 * This test class uses PICT (Pairwise Independent Combinatorial Testing) to
 * generate
 * test cases that cover all pairwise combinations of search parameters.
 * 
 * PICT Model: tqsbackend/pict-models/item_search.pict
 * Generated Cases: 32 test combinations
 * 
 * Parameters tested:
 * - keyword: NULL, "Party", "", "Special!@#"
 * - category: NULL, "Lighting", "Party", "Electronics", "InvalidCategory"
 * - minPrice: NULL, "0", "50", "100"
 * - maxPrice: NULL, "100", "200", "500"
 * - minRating: NULL, "0.0", "2.5", "4.0", "5.5"
 * - location: NULL, "Lisbon", "Porto", "Aveiro", ""
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ItemSearchPICTTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    public void setUp() {
        // Clean database
        bookingRepository.deleteAll();
        ratingRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test data
        User owner = new User();
        owner.setName("PICT Test Owner");
        owner.setEmail("pict-owner@test.com");
        owner.setPassword("password");
        owner.setRole(UserRoles.OWNER);
        userRepository.save(owner);

        // Create categories
        createCategory("Lighting");
        createCategory("Party");
        createCategory("Electronics");

        // Create items with various properties
        createItem("Party Lights LED", "Lighting", 100.0, "Lisbon", 4.5, owner.getId());
        createItem("Party Hat Pack", "Party", 50.0, "Porto", 2.5, owner.getId());
        createItem("Speaker System", "Electronics", 200.0, "Aveiro", 4.0, owner.getId());
        createItem("Disco Ball", "Party", 30.0, "Lisbon", 5.0, owner.getId());
    }

    private void createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
    }

    private void createItem(String name, String categoryName, Double price,
            String location, Double rating, Long ownerId) {
        Category category = categoryRepository.findByName(categoryName);
        if (category == null)
            return;

        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setCategory(category);
        item.setLocation(location);
        item.setAverageRating(rating);
        item.setOwnerId(ownerId);
        item.setDescription("Test item");
        item.setImageUrl("https://example.com/image.jpg");
        itemRepository.save(item);
    }

    /**
     * PICT Test: Tests all pairwise combinations of search parameters
     * 
     * CSV Format: keyword,category,minPrice,maxPrice,minRating,location
     * Test count: 32 combinations
     */
    @ParameterizedTest(name = "#{index}: keyword={0}, category={1}, minPrice={2}, maxPrice={3}, minRating={4}, location={5}")
    @CsvFileSource(resources = "/pict/item_search_cases.csv", numLinesToSkip = 1)
    public void testItemSearchPICT(String keyword, String category, String minPrice,
            String maxPrice, String minRating, String location) throws Exception {
        // Build request
        var request = get("/items/search");

        // Add parameters if not NULL
        if (!"NULL".equals(keyword)) {
            request.param("q", keyword);
        }
        if (!"NULL".equals(category) && category != null && !category.isEmpty()) {
            // Remove quotes from category value
            String cleanCategory = category.replace("\"", "");
            request.param("category", cleanCategory);
        }
        if (!"NULL".equals(minPrice)) {
            String cleanMinPrice = minPrice.replace("\"", "");
            request.param("minPrice", cleanMinPrice);
        }
        if (!"NULL".equals(maxPrice)) {
            String cleanMaxPrice = maxPrice.replace("\"", "");
            request.param("maxPrice", cleanMaxPrice);
        }
        if (!"NULL".equals(minRating)) {
            String cleanMinRating = minRating.replace("\"", "");
            request.param("minRating", cleanMinRating);
        }
        if (!"NULL".equals(location) && location != null) {
            String cleanLocation = location.replace("\"", "");
            if (!cleanLocation.isEmpty()) {
                request.param("location", cleanLocation);
            }
        }

        // Execute request and verify response
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("categories"));
    }
}
