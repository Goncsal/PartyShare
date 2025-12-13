package tqs.backend.tqsbackend.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.service.BookingService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CounterOfferSteps {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User renter;
    private User owner;
    private Item item;
    private Booking currentBooking;
    private Booking updatedBooking;

    @Before
    public void setup() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Given("I have a pending booking for {string} from {string} to {string}")
    public void i_have_a_pending_booking_for_from_to(String itemName, String startDate, String endDate) {
        // Assume renter, owner, and item are already set up by previous steps (reusing
        // logic or relying on shared context if possible, but here explicit for
        // isolation)
        // For simplicity in this isolated step class, we'll ensure they exist if not
        // already set (though in a real suite we might share state)
        if (renter == null) {
            renter = userRepository.findByEmail("renter@email.com").orElseGet(() -> {
                User u = new User("Renter", "renter@email.com", "pass", UserRoles.RENTER);
                return userRepository.save(u);
            });
        }
        if (owner == null) {
            owner = userRepository.findByEmail("owner@email.com").orElseGet(() -> {
                User u = new User("Owner", "owner@email.com", "pass", UserRoles.OWNER);
                return userRepository.save(u);
            });
        }
        if (item == null) {
            item = itemRepository.findByName(itemName).stream().findFirst().orElseGet(() -> {
                Category cat = categoryRepository.findByName("MUSIC");
                if (cat == null) {
                    cat = new Category("MUSIC");
                    categoryRepository.save(cat);
                }
                Item i = new Item();
                i.setName(itemName);
                i.setPrice(50.0);
                i.setCategory(cat);
                i.setOwnerId(owner.getId());
                i.setActive(true);
                return itemRepository.save(i);
            });
        }

        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setRenterId(renter.getId());
        request.setStartDate(LocalDate.parse(startDate));
        request.setEndDate(LocalDate.parse(endDate));
        currentBooking = bookingService.createBooking(request);
    }

    @When("the owner makes a counter offer of {double} for the booking")
    public void the_owner_makes_a_counter_offer_of_for_the_booking(Double newPrice) {
        updatedBooking = bookingService.counterOfferBooking(currentBooking.getId(), newPrice, owner.getId());
    }

    @Then("the booking status should be {string}")
    public void the_booking_status_should_be(String status) {
        // Refresh booking from DB to be sure
        updatedBooking = bookingRepository.findById(currentBooking.getId()).get();
        assertEquals(BookingStatus.valueOf(status), updatedBooking.getStatus());
    }

    @Then("the booking daily price should be {double}")
    public void the_booking_daily_price_should_be(Double price) {
        assertEquals(price, updatedBooking.getDailyPrice().doubleValue());
    }

    @Given("the owner has made a counter offer of {double}")
    public void the_owner_has_made_a_counter_offer_of(Double price) {
        // Booking is already created by Background step
        the_owner_makes_a_counter_offer_of_for_the_booking(price);
    }

    @When("the renter accepts the counter offer")
    public void the_renter_accepts_the_counter_offer() {
        updatedBooking = bookingService.acceptCounterOffer(currentBooking.getId(), renter.getId());
    }

    @Then("the booking total price should be calculated based on {double}")
    public void the_booking_total_price_should_be_calculated_based_on(Double dailyPrice) {
        long days = ChronoUnit.DAYS.between(updatedBooking.getStartDate(), updatedBooking.getEndDate());
        Double expectedTotal = dailyPrice * days;
        assertEquals(expectedTotal, updatedBooking.getTotalPrice().doubleValue());
    }

    @When("the renter declines the counter offer")
    public void the_renter_declines_the_counter_offer() {
        updatedBooking = bookingService.declineCounterOffer(currentBooking.getId(), renter.getId());
    }
}
