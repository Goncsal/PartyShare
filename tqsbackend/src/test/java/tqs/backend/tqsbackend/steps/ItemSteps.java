package tqs.backend.tqsbackend.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.service.ItemService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ItemSteps {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private tqs.backend.tqsbackend.repository.BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private tqs.backend.tqsbackend.service.UserService userService;

    private User currentUser;
    private Item createdItem;
    private List<Item> searchResults;

    @Before
    public void setup() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Given("I am a registered user with email {string}")
    public void i_am_a_registered_user_with_email(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            userService.registerUser("Owner User", email, "password", UserRoles.OWNER);
        }
        currentUser = userRepository.findByEmail(email).get();
    }

    @When("I create an item with name {string}, price {double}, and category {string}")
    public void i_create_an_item(String name, Double price, String categoryName) {
        Category category = categoryRepository.findByName(categoryName);
        if (category == null) {
            category = new Category(categoryName);
            categoryRepository.save(category);
        }

        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setCategory(category);
        item.setOwnerId(currentUser.getId());
        item.setActive(true);

        createdItem = itemService.createItem(item);
    }

    @Then("the item should be created successfully")
    public void the_item_should_be_created_successfully() {
        assertNotNull(createdItem);
        assertNotNull(createdItem.getId());
    }

    @Then("the item should be available in the system")
    public void the_item_should_be_available_in_the_system() {
        assertTrue(itemRepository.findById(createdItem.getId()).isPresent());
    }

    @Given("the following categories exist:")
    public void the_following_categories_exist(io.cucumber.datatable.DataTable dataTable) {
        List<String> categories = dataTable.asList(String.class);
        for (String categoryName : categories) {
            if (categoryRepository.findByName(categoryName) == null) {
                categoryRepository.save(new Category(categoryName));
            }
        }
    }

    @Given("the following items exist:")
    public void the_following_items_exist(io.cucumber.datatable.DataTable dataTable) {
        // Ensure we have an owner
        if (currentUser == null) {
            i_am_a_registered_user_with_email("default_owner@email.com");
        }

        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : data) {
            String categoryName = row.get("category");
            Category category = categoryRepository.findByName(categoryName);
            if (category == null) {
                category = new Category(categoryName);
                categoryRepository.save(category);
            }

            Item item = new Item();
            item.setName(row.get("name"));
            item.setPrice(Double.parseDouble(row.get("price")));
            item.setCategory(category);
            item.setOwnerId(currentUser.getId());
            item.setActive(true);
            itemRepository.save(item);
        }
    }

    @When("I search for items with keyword {string}")
    public void i_search_for_items_with_keyword(String keyword) {
        searchResults = itemService.searchItems(keyword, null, null, null, null, null);
    }

    @Then("I should find {int} item")
    public void i_should_find_item(int count) {
        assertEquals(count, searchResults.size());
    }

    @Then("the item name should be {string}")
    public void the_item_name_should_be(String name) {
        assertEquals(name, searchResults.get(0).getName());
    }
}
