package tqs.backend.tqsbackend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.fixtures.BookingTestFixtures;
import tqs.backend.tqsbackend.service.BookingService;

/**
 * TDD tests for Cancel Rental REST endpoints (US 1.7)
 */
@WebMvcTest(BookingRestController.class)
@Import(IT_CancelRentalRestControllerTest.TestConfig.class)
class IT_CancelRentalRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingService bookingService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BookingService bookingService() {
            return Mockito.mock(BookingService.class);
        }
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/cancel returns 200 when successful")
    void cancelBooking_success() throws Exception {
        Long bookingId = 1L;
        Long renterId = 70L;
        
        Booking cancelledBooking = BookingTestFixtures.sampleBooking(bookingId, BookingTestFixtures.sampleItem(10L));
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        
        when(bookingService.cancelBooking(bookingId, renterId)).thenReturn(cancelledBooking);

        mockMvc.perform(post("/api/bookings/{id}/cancel", bookingId)
                .param("renterId", renterId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancelBooking(bookingId, renterId);
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/cancel returns 400 when booking not found")
    void cancelBooking_notFound() throws Exception {
        Long bookingId = 999L;
        Long renterId = 70L;
        
        when(bookingService.cancelBooking(bookingId, renterId))
                .thenThrow(new BookingValidationException("Booking not found"));

        mockMvc.perform(post("/api/bookings/{id}/cancel", bookingId)
                .param("renterId", renterId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/bookings/{id}/cancel returns 400 when wrong renter")
    void cancelBooking_wrongRenter() throws Exception {
        Long bookingId = 1L;
        Long wrongRenterId = 999L;
        
        when(bookingService.cancelBooking(bookingId, wrongRenterId))
                .thenThrow(new BookingValidationException("You can only cancel your own bookings"));

        mockMvc.perform(post("/api/bookings/{id}/cancel", bookingId)
                .param("renterId", wrongRenterId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
