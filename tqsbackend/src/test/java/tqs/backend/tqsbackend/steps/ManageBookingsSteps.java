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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.pages.BookingRequestsPage;
import tqs.backend.tqsbackend.pages.DashboardPage;
import tqs.backend.tqsbackend.pages.LoginPage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManageBookingsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    private LoginPage loginPage;
    private DashboardPage dashboardPage;
    private BookingRequestsPage bookingRequestsPage;

    @Before("@manage_bookings")
    public void setUp() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        }
        context = browser.newContext();
        page = context.newPage();

        loginPage = new LoginPage(page);
        dashboardPage = new DashboardPage(page);
        bookingRequestsPage = new BookingRequestsPage(page);
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    @Given("a pending booking request exists for {string} from {string}")
    public void a_pending_booking_request_exists_for_from(String itemName, String renterEmail) {
        User renter = userRepository.findByEmail(renterEmail).orElseThrow();
        // Use findByNameContainingIgnoreCase which exists in repository
        List<Item> items = itemRepository.findByNameContainingIgnoreCase(itemName);
        if (items.isEmpty()) {
            throw new RuntimeException("Item not found: " + itemName);
        }
        Item item = items.get(0);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setRenterId(renter.getId()); // Use renterId instead of setRenter
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(3));
        booking.setStatus(tqs.backend.tqsbackend.entity.BookingStatus.REQUESTED); // Use Enum REQUESTED
        booking.setPaymentStatus(tqs.backend.tqsbackend.entity.PaymentStatus.PENDING); // Set required payment status
        booking.setDailyPrice(java.math.BigDecimal.valueOf(item.getPrice())); // Use BigDecimal
        booking.setTotalPrice(java.math.BigDecimal.valueOf(item.getPrice() * 3)); // Set total price

        bookingRepository.save(booking);
    }

    @Given("I login as an owner with email {string} and password {string}")
    public void i_login_as_an_owner(String email, String password) {
        loginPage.navigate("http://localhost:" + port + "/users/login");
        loginPage.login(email, password);
        // Wait for search page to load
        page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Find Items")).waitFor();
    }

    @Given("I navigate to the dashboard")
    public void i_navigate_to_the_dashboard() {
        if (!page.url().contains("/items/search")) {
            dashboardPage.navigate("http://localhost:" + port + "/items/search");
        }
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Dashboard")).click();
    }

    @When("I click on the dashboard link {string}")
    public void i_click_on_dashboard_link(String linkName) {
        if (linkName.equals("My BookingRequests")) {
            dashboardPage.clickMyBookingRequests();
        }
    }

    @When("I accept the booking request for {string}")
    public void i_accept_the_booking_request_for(String itemName) {
        bookingRequestsPage.acceptRequest(itemName);
    }

    @Then("the booking request for {string} should be {string}")
    public void the_booking_request_for_should_be(String itemName, String status) {
        if ("ACCEPTED".equals(status)) {
            i_navigate_to_the_dashboard();
            assertTrue(dashboardPage.isBookingStatus(itemName, status));
        } else {
            assertTrue(bookingRequestsPage.hasRequestStatus(itemName, status));
        }
    }
}
