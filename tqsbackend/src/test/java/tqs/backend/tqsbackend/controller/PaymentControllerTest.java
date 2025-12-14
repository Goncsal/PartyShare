package tqs.backend.tqsbackend.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;
import tqs.backend.tqsbackend.service.BookingService;
import tqs.backend.tqsbackend.service.WalletService;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private WalletService walletService;

    private Booking createTestBooking() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(10.0);
        item.setCategory(category);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setRenterId(1L);
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(3));
        booking.setDailyPrice(BigDecimal.valueOf(10.0));
        booking.setTotalPrice(BigDecimal.valueOf(20.0));
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        return booking;
    }

    @Test
    void showPaymentPage_WithValidBooking_ReturnsCheckoutView() throws Exception {
        Booking booking = createTestBooking();
        when(bookingService.getBooking(1L)).thenReturn(booking);

        mockMvc.perform(get("/payment/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/checkout"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    void showPaymentPage_WithNullBooking_RedirectsToBookings() throws Exception {
        when(bookingService.getBooking(anyLong())).thenReturn(null);

        mockMvc.perform(get("/payment/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings?error=Booking not found"));
    }

    @Test
    void paymentSuccess_WithValidBooking_ReturnsSuccessView() throws Exception {
        Booking booking = createTestBooking();
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaymentReference("ref-123");
        when(bookingService.getBooking(1L)).thenReturn(booking);

        mockMvc.perform(get("/payment/success/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/success"))
                .andExpect(model().attributeExists("booking"));
        
        // Verify confirmPayment was called to update booking status
        org.mockito.Mockito.verify(bookingService).confirmPayment(1L);
    }

    @Test
    void paymentCancel_WithValidBooking_ReturnsCancelView() throws Exception {
        Booking booking = createTestBooking();
        when(bookingService.getBooking(1L)).thenReturn(booking);

        mockMvc.perform(get("/payment/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/cancel"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    void createCheckoutSession_WithNullBooking_ReturnsBadRequest() throws Exception {
        when(bookingService.getBooking(anyLong())).thenReturn(null);

        mockMvc.perform(post("/payment/create-checkout-session/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCheckoutSession_WithEmptyStripeKey_ReturnsBadRequest() throws Exception {
        Booking booking = createTestBooking();
        when(bookingService.getBooking(1L)).thenReturn(booking);

        // Stripe key is empty, so it should fail
        mockMvc.perform(post("/payment/create-checkout-session/1"))
                .andExpect(status().isBadRequest());
    }
}
