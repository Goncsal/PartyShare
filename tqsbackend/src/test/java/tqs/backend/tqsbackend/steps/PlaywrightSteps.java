package tqs.backend.tqsbackend.steps;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.tqsbackend.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaywrightSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @Before(value = "@owner_flow or @renter_search or @admin_user_management or @rental", order = 1)
    public void setUp() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        }
        context = browser.newContext();
        page = context.newPage();
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Given("I am on the home page")
    public void i_am_on_the_home_page() {
        page.navigate("http://localhost:" + port + "/items/search");
    }

    @When("I navigate to the registration page")
    public void i_navigate_to_the_registration_page() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Register")).click();
    }

    @When("I register in the UI with name {string}, email {string}, password {string}, and role {string}")
    public void i_register_in_the_ui_with_details(String name, String email, String password, String role) {
        // Clean up existing user if any
        if (userRepository.findByEmail(email).isPresent()) {
            userRepository.delete(userRepository.findByEmail(email).get());
        }

        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Name")).fill(name);
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email")).fill(email);
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(password);
        page.getByLabel("Role").selectOption(role);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Account")).click();
    }

    @Then("I should be redirected to the login page")
    public void i_should_be_redirected_to_the_login_page() {
        // Check if we are on the login page or if the login form is visible
        // The URL might contain /login or the page has "Login" button
        assertTrue(page.url().contains("/login")
                || page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).isVisible());
    }

    @When("I login with email {string} and password {string}")
    public void i_login_with_email_and_password(String email, String password) {
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email")).fill(email);
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
    }

    @Then("I should be on the home page")
    public void i_should_be_on_the_home_page() {
        // After login, we expect to be redirected to home or dashboard
        // Let's check if "Logout" is visible or URL is home
        // page.waitForURL("**/items/search"); // Optional wait
        assertTrue(page.url().contains("/items/search")
                || page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Logout")).isVisible());
    }

    @When("I search for {string} in the UI")
    public void i_search_for_in_the_ui(String keyword) {
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("What are you looking for?")).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("What are you looking for?"))
                .fill(keyword);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Filter")).click();
    }

    @When("I filter by max price {string}")
    public void i_filter_by_max_price(String price) {
        page.getByPlaceholder("€").click();
        page.getByPlaceholder("€").fill(price);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Filter")).click();
    }

    @When("I click on the item details")
    public void i_click_on_the_item_details() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Details")).first().click();
    }

    @Then("I should be on the item details page")
    public void i_should_be_on_the_item_details_page() {
        assertTrue(page.url().contains("/items/"));
        assertTrue(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Details")).isVisible() ||
                page.getByText("Description").isVisible());
    }

    public Page getPage() {
        return page;
    }
}
