package tqs.backend.tqsbackend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.CategoryRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.service.PaymentResult;
import tqs.backend.tqsbackend.service.PaymentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IT_BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private PaymentService paymentService;

    @Test
    void createBooking_WithOffer_CreatesBookingWithRequestedStatus() throws Exception {
        User owner = new User();
        owner.setEmail("owner_b@example.com");
        owner.setPassword("password");
        owner.setName("OwnerB");
        owner.setRole(UserRoles.OWNER);
        userRepository.save(owner);

        User renter = new User();
        renter.setEmail("renter_b@example.com");
        renter.setPassword("password");
        renter.setName("RenterB");
        renter.setRole(UserRoles.RENTER);
        userRepository.save(renter);

        Category category = new Category();
        category.setName("Books");
        categoryRepository.save(category);

        Item item = new Item();
        item.setName("Book");
        item.setDescription("A good book");
        item.setPrice(10.0);
        item.setCategory(category);
        item.setOwnerId(owner.getId());
        item.setLocation("Aveiro");
        itemRepository.save(item);

        mockMvc.perform(post("/bookings")
                .sessionAttr("userId", renter.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("itemId", item.getId().toString())
                .param("startDate", LocalDate.now().plusDays(1).toString())
                .param("endDate", LocalDate.now().plusDays(3).toString())
                .param("proposedPrice", "8.0")) // Offer lower price
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));

        List<Booking> bookings = bookingRepository.findByRenterId(renter.getId());
        assertThat(bookings).hasSize(1);
        Booking booking = bookings.get(0);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(booking.getDailyPrice().doubleValue()).isEqualTo(8.0);
    }
}
