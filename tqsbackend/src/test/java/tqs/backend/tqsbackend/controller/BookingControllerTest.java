package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.BookingService;
import tqs.backend.tqsbackend.service.ItemService;

import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private ItemService itemService;

    @Test
    void showRentForm_LoggedIn() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        tqs.backend.tqsbackend.entity.Category category = new tqs.backend.tqsbackend.entity.Category();
        category.setName("Electronics");
        item.setCategory(category);

        when(itemService.getItemById(1L)).thenReturn(item);

        mvc.perform(get("/bookings/rent/1").sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/rent_item"))
                .andExpect(model().attributeExists("item"))
                .andExpect(model().attributeExists("bookingRequest"));
    }

    @Test
    void showRentForm_LoggedOut() throws Exception {
        mvc.perform(get("/bookings/rent/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void createBooking_LoggedIn_Success() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setPaymentReference("REF123");

        when(bookingService.createBooking(any(BookingCreateRequest.class))).thenReturn(booking);

        mvc.perform(post("/bookings")
                .sessionAttr("userId", 1L)
                .param("itemId", "1")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-01-05"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/1"));

        verify(bookingService).createBooking(any(BookingCreateRequest.class));
    }

    @Test
    void createBooking_LoggedOut() throws Exception {
        mvc.perform(post("/bookings")
                .param("itemId", "1")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-05"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void getUserBookings_LoggedIn() throws Exception {
        Item item = new Item();
        item.setName("Test Item");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setStatus(tqs.backend.tqsbackend.entity.BookingStatus.CONFIRMED);
        List<Booking> bookings = Arrays.asList(booking);

        when(bookingService.getBookingsForRenter(1L)).thenReturn(bookings);

        mvc.perform(get("/bookings").sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/list"))
                .andExpect(model().attribute("bookings", bookings));
    }

    @Test
    void getUserBookings_LoggedOut() throws Exception {
        mvc.perform(get("/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void createBooking_WithValidationErrors() throws Exception {
        mvc.perform(post("/bookings")
                .sessionAttr("userId", 1L)
                .param("itemId", "1")
                .param("startDate", "invalid-date") // Invalid date format
                .param("endDate", "2026-01-05"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void createBooking_WithNullDates() throws Exception {
        mvc.perform(post("/bookings")
                .sessionAttr("userId", 1L)
                .param("itemId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }
}
