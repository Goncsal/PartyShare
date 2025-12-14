package tqs.backend.tqsbackend.steps;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.tqsbackend.pages.CreateItemPage;
import tqs.backend.tqsbackend.pages.DashboardPage;
import tqs.backend.tqsbackend.pages.LoginPage;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateItemSteps {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private CreateItemPage createItemPage;

    @Before("@create_item")
    public void setUp() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        }
        context = browser.newContext();
        page = context.newPage();

        loginPage = new LoginPage(page);
        dashboardPage = new DashboardPage(page);
        createItemPage = new CreateItemPage(page);
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
        }
        if (playwright != null) {
            playwright.close();
            playwright = null; // Ensure fresh instance for next test
        }
    }

    @Given("I am logged in as an owner with email {string} and password {string}")
    public void i_am_logged_in_as_an_owner(String email, String password) {
        // Assuming user exists (handled by UserSteps or data seeding)
        loginPage.navigate("http://localhost:" + port + "/users/login");
        loginPage.login(email, password);
    }

    @Given("I am on the dashboard page")
    public void i_am_on_the_dashboard_page() {
        // After login, we might be on dashboard or home. Ensure we are where we need to
        // be.
        // If login redirects to home, we might need to navigate to dashboard explicitly
        // if it's a separate page.
        // For now, let's assume login redirects to a place where "Add New Item" is
        // visible, or we navigate there.
        // If "Add New Item" is on the items/search page (which seems to be the home),
        // we are good.
        // The script navigates to /items/search then clicks Dashboard then Add New
        // Item.
        // Let's follow the script's logic if needed, or just check for the button.
        // Script: page.navigate("http://localhost:8080/items/search");
        // page.getByRole(AriaRole.LINK, new
        // Page.GetByRoleOptions().setName("Dashboard")).click();

        // Let's assume we are on a page that has the Dashboard link or we are already
        // there.
        // If login redirects to /items/search:
        if (!page.url().contains("/items/search")) {
            dashboardPage.navigate("http://localhost:" + port + "/items/search");
        }
        // Click Dashboard if not already there (assuming Dashboard is a separate page
        // or a view)
        // The script clicks "Dashboard" link.
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Dashboard")).click();
    }

    @When("I click on {string}")
    public void i_click_on(String buttonName) {
        if (buttonName.equals("Add New Item")) {
            dashboardPage.clickAddNewItem();
        }
    }

    @When("I fill the item form with:")
    public void i_fill_the_item_form_with(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        createItemPage.fillForm(
                data.get("name"),
                data.get("description"),
                data.get("price"),
                data.get("category"),
                data.get("location"),
                data.get("image"));
    }

    @When("I submit the item form")
    public void i_submit_the_item_form() {
        createItemPage.submit();
    }

    @Then("I should see the item {string} in my items list")
    public void i_should_see_the_item_in_my_items_list(String itemName) {
        assertTrue(dashboardPage.hasItem(itemName));
    }
}
