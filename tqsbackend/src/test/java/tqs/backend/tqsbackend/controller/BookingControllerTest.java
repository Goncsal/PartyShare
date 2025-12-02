package tqs.backend.tqsbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.fixtures.BookingTestFixtures;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;
import tqs.backend.tqsbackend.exception.AvailabilityException;
import tqs.backend.tqsbackend.service.BookingService;
import tqs.backend.tqsbackend.service.ItemService;

@WebMvcTest(BookingController.class)
@SuppressWarnings("removal")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private ItemService itemService;

    @Test
    void showRentForm_populatesModel() throws Exception {
        Item item = BookingTestFixtures.sampleItem(11L);

        when(itemService.getItemById(11L)).thenReturn(item);

        mockMvc.perform(get("/bookings/rent/{itemId}", 11L))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/rent_item"))
                .andExpect(model().attributeExists("bookingRequest"))
                .andExpect(model().attribute("item", item));
    }

    @Test
    void createBooking_successRedirectsWithFlash() throws Exception {
        Item item = BookingTestFixtures.sampleItem(11L);
        when(itemService.getItemById(11L)).thenReturn(item);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaymentReference("PAY-CTRL");

        when(bookingService.createBooking(any())).thenReturn(booking);

        mockMvc.perform(post("/bookings")
                .param("itemId", "11")
                .param("renterId", "99")
                .param("startDate", LocalDate.now().plusDays(1).toString())
                .param("endDate", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/rent/11"))
                .andExpect(flash().attribute("success", "Booking confirmed! Ref: PAY-CTRL"));
    }

    @Test
    void createBooking_availabilityIssuesAddsErrorFlash() throws Exception {
        Item item = BookingTestFixtures.sampleItem(11L);
        when(itemService.getItemById(11L)).thenReturn(item);

        when(bookingService.createBooking(any())).thenThrow(new AvailabilityException("conflict"));

        mockMvc.perform(post("/bookings")
                .param("itemId", "11")
                .param("renterId", "99")
                .param("startDate", LocalDate.now().plusDays(1).toString())
                .param("endDate", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/rent/11"))
                .andExpect(flash().attribute("error", "Dates unavailable for this item"));
    }
}
