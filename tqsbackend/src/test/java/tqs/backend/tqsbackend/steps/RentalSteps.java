package tqs.backend.tqsbackend.steps;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.tqsbackend.pages.DetailPage;
import tqs.backend.tqsbackend.pages.LoginPage;
import tqs.backend.tqsbackend.pages.RentalFormPage;
import tqs.backend.tqsbackend.pages.SearchPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RentalSteps {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private SearchPage searchPage;
    private DetailPage detailPage;
    private RentalFormPage rentalFormPage;
    private LoginPage loginPage;

    @Before("@rental")
    public void setUp() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        }
        context = browser.newContext();
        page = context.newPage();

        searchPage = new SearchPage(page);
        detailPage = new DetailPage(page);
        rentalFormPage = new RentalFormPage(page);
        loginPage = new LoginPage(page);
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Given("I am logged in as a renter with email {string} and password {string}")
    public void i_am_logged_in_as_a_renter(String email, String password) {
        // Ensure user exists (optional if handled by other steps, but good for
        // robustness)
        // Assuming user exists in DB via other steps
        loginPage.navigate("http://localhost:" + port + "/users/login");
        loginPage.login(email, password);
    }

    @Given("I am on the search page")
    public void i_am_on_the_search_page() {
        searchPage.navigate("http://localhost:" + port + "/items/search");
    }

    @When("I select the first item to view details")
    public void i_select_the_first_item_to_view_details() {
        searchPage.clickFirstItemDetails();
    }

    @When("I choose to rent the item")
    public void i_choose_to_rent_the_item() {
        detailPage.clickRentNow();
    }

    @When("I fill in the rental dates from {string} to {string}")
    public void i_fill_in_the_rental_dates(String startDate, String endDate) {
        rentalFormPage.fillStartDate(startDate);
        rentalFormPage.fillEndDate(endDate);
    }

    @When("I make an offer of {string} per day")
    public void i_make_an_offer_of_per_day(String amount) {
        rentalFormPage.fillOffer(amount);
    }

    @When("I submit the rental request")
    public void i_submit_the_rental_request() {
        rentalFormPage.submit();
    }

    @Then("I should see the confirmation message {string}")
    public void i_should_see_the_confirmation_message(String message) {
        assertTrue(rentalFormPage.hasConfirmationMessage(message));
    }
}
