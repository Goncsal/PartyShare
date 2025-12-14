package tqs.backend.tqsbackend.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.service.BookingService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingSteps {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private tqs.backend.tqsbackend.repository.CategoryRepository categoryRepository;

    private User renter;
    private User owner;
    private Item item;
    private Booking createdBooking;
    private Exception bookingException;

    @Given("a renter exists with email {string}")
    public void a_renter_exists_with_email(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User("Renter User", email, "password", UserRoles.RENTER);
            userRepository.save(user);
        }
        renter = userRepository.findByEmail(email).get();
    }

    @Given("an item exists with name {string}, price {double}, and owner {string}")
    public void an_item_exists_with_name_price_and_owner(String name, Double price, String ownerEmail) {
        if (userRepository.findByEmail(ownerEmail).isEmpty()) {
            User user = new User("Owner User", ownerEmail, "password", UserRoles.OWNER);
            userRepository.save(user);
        }
        owner = userRepository.findByEmail(ownerEmail).get();

        Category category = categoryRepository.findByName("ELECTRONICS");
        if (category == null) {
            category = new Category("ELECTRONICS");
            categoryRepository.save(category);
        }

        // Cleanup existing items with same name
        java.util.List<Item> existingItems = itemRepository.findByName(name);
        for (Item existing : existingItems) {
            java.util.List<Booking> bookings = bookingRepository.findByItemId(existing.getId());
            bookingRepository.deleteAll(bookings);
            itemRepository.delete(existing);
        }

        item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setCategory(category); // Default category
        item.setOwnerId(owner.getId());
        item.setActive(true);
        itemRepository.save(item);
    }

    @When("I book the item {string} from {string} to {string}")
    public void i_book_the_item_from_to(String itemName, String startDate, String endDate) {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(item.getId());
        request.setRenterId(renter.getId());
        request.setStartDate(LocalDate.parse(startDate));
        request.setEndDate(LocalDate.parse(endDate));

        try {
            createdBooking = bookingService.createBooking(request);
        } catch (Exception e) {
            bookingException = e;
        }
    }

    @Then("the booking should be created successfully")
    public void the_booking_should_be_created_successfully() {
        assertNull(bookingException);
        assertNotNull(createdBooking);
        assertNotNull(createdBooking.getId());
    }

    @Then("the total price should be {double}")
    public void the_total_price_should_be(Double expectedTotal) {
        assertEquals(expectedTotal, createdBooking.getTotalPrice().doubleValue());
    }

    @Given("the item {string} is already booked from {string} to {string}")
    public void the_item_is_already_booked_from_to(String itemName, String startDate, String endDate) {
        Booking booking = new Booking(item, renter.getId(), LocalDate.parse(startDate), LocalDate.parse(endDate),
                java.math.BigDecimal.valueOf(item.getPrice()), java.math.BigDecimal.valueOf(100.0),
                BookingStatus.ACCEPTED, PaymentStatus.PENDING);
        bookingRepository.save(booking);
    }

    @When("I try to book the item {string} from {string} to {string}")
    public void i_try_to_book_the_item_from_to(String itemName, String startDate, String endDate) {
        i_book_the_item_from_to(itemName, startDate, endDate);
    }

    @Then("the booking should fail with error {string}")
    public void the_booking_should_fail_with_error(String errorMessage) {
        assertNotNull(bookingException);
        assertEquals(errorMessage, bookingException.getMessage());
    }
}
