package tqs.backend.tqsbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;
import tqs.backend.tqsbackend.exception.PaymentException;
import tqs.backend.tqsbackend.service.BookingService;

@WebMvcTest(BookingRestController.class)
@SuppressWarnings("removal")
class BookingRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_returnsCreatedPayload() throws Exception {
        Item item = new Item();
        item.setId(10L);
        item.setPrice(40.0);

        Booking booking = new Booking(item, 70L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(item.getPrice()), BigDecimal.valueOf(80), BookingStatus.CONFIRMED,
                PaymentStatus.PAID);
        booking.setId(1L);
        booking.setPaymentReference("PAY-API");

        when(bookingService.createBooking(any(BookingCreateRequest.class))).thenReturn(booking);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"itemId\": 10,
                          \"renterId\": 70,
                          \"startDate\": \"%s\",
                          \"endDate\": \"%s\"
                        }
                        """.formatted(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paymentReference").value("PAY-API"));
    }

    @Test
    void createBooking_paymentFailureReturnsErrorStatus() throws Exception {
        when(bookingService.createBooking(any(BookingCreateRequest.class)))
                .thenThrow(new PaymentException("declined"));

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"itemId\": 10,
                          \"renterId\": 70,
                          \"startDate\": \"%s\",
                          \"endDate\": \"%s\"
                        }
                        """.formatted(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3))))
                        .andExpect(status().isPaymentRequired());
    }

    @Test
    void getBooking_returnsNotFoundWhenMissing() throws Exception {
        when(bookingService.getBooking(999L)).thenReturn(null);

        mockMvc.perform(get("/api/bookings/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooking_returnsPayloadWhenPresent() throws Exception {
        Item item = new Item();
        item.setId(10L);
        Booking booking = new Booking(item, 70L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(40), BigDecimal.valueOf(80), BookingStatus.CONFIRMED, PaymentStatus.PAID);
        booking.setId(15L);
        booking.setPaymentReference("PAY-GET");

        when(bookingService.getBooking(15L)).thenReturn(booking);

        mockMvc.perform(get("/api/bookings/{id}", 15L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.paymentReference").value("PAY-GET"));

        verify(bookingService).getBooking(15L);
    }
}
