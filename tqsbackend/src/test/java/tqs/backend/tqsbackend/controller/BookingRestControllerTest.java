package tqs.backend.tqsbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.fixtures.BookingTestFixtures;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.exception.PaymentException;
import tqs.backend.tqsbackend.service.BookingService;

@WebMvcTest(BookingRestController.class)
@Import(BookingRestControllerTest.TestConfig.class)
class BookingRestControllerTest {

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
        void createBooking_returnsCreatedPayload() throws Exception {
                Booking booking = BookingTestFixtures.sampleBooking(1L, BookingTestFixtures.sampleItem(10L));
                booking.setPaymentReference("PAY-API");
                booking.setStatus(tqs.backend.tqsbackend.entity.BookingStatus.REQUESTED);
                booking.setPaymentStatus(tqs.backend.tqsbackend.entity.PaymentStatus.PENDING);

                when(bookingService.createBooking(any(BookingCreateRequest.class))).thenReturn(booking);

                mockMvc.perform(post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BookingTestFixtures.bookingRequestJson(10L, 70L, LocalDate.now().plusDays(1),
                                                LocalDate.now().plusDays(3))))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status", is("REQUESTED")))
                                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                                .andExpect(jsonPath("$.paymentReference").value("PAY-API"));
        }

        @Test
        void createBooking_paymentFailureReturnsErrorStatus() throws Exception {
                when(bookingService.createBooking(any(BookingCreateRequest.class)))
                                .thenThrow(new PaymentException("declined"));

                mockMvc.perform(post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BookingTestFixtures.bookingRequestJson(10L, 70L, LocalDate.now().plusDays(1),
                                                LocalDate.now().plusDays(3))))
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
                Booking booking = BookingTestFixtures.sampleBooking(15L, BookingTestFixtures.sampleItem(10L));
                booking.setPaymentReference("PAY-GET");

                when(bookingService.getBooking(15L)).thenReturn(booking);

                mockMvc.perform(get("/api/bookings/{id}", 15L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(15))
                                .andExpect(jsonPath("$.paymentReference").value("PAY-GET"));

                verify(bookingService).getBooking(15L);
        }

        @Test
        void acceptBooking_returnsUpdatedBooking() throws Exception {
                Booking booking = BookingTestFixtures.sampleBooking(1L, BookingTestFixtures.sampleItem(10L));
                booking.setStatus(tqs.backend.tqsbackend.entity.BookingStatus.ACCEPTED);

                when(bookingService.acceptBooking(1L, 70L)).thenReturn(booking);

                mockMvc.perform(post("/api/bookings/{id}/accept", 1L)
                                .param("ownerId", "70"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("ACCEPTED")));
        }

        @Test
        void declineBooking_returnsUpdatedBooking() throws Exception {
                Booking booking = BookingTestFixtures.sampleBooking(1L, BookingTestFixtures.sampleItem(10L));
                booking.setStatus(tqs.backend.tqsbackend.entity.BookingStatus.REJECTED);

                when(bookingService.declineBooking(1L, 70L)).thenReturn(booking);

                mockMvc.perform(post("/api/bookings/{id}/decline", 1L)
                                .param("ownerId", "70"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("REJECTED")));
        }

        @Test
        void counterOfferBooking_returnsUpdatedBooking() throws Exception {
                Booking booking = BookingTestFixtures.sampleBooking(1L, BookingTestFixtures.sampleItem(10L));
                booking.setStatus(tqs.backend.tqsbackend.entity.BookingStatus.COUNTER_OFFER);

                when(bookingService.counterOfferBooking(1L, 50.0, 70L)).thenReturn(booking);

                mockMvc.perform(post("/api/bookings/{id}/counter-offer", 1L)
                                .param("newPrice", "50.0")
                                .param("ownerId", "70"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("COUNTER_OFFER")));
        }

        @Test
        void getBookingRequests_returnsList() throws Exception {
                Booking booking = BookingTestFixtures.sampleBooking(1L, BookingTestFixtures.sampleItem(10L));
                when(bookingService.getPendingBookingsByOwner(70L)).thenReturn(java.util.Collections.singletonList(booking));

                mockMvc.perform(get("/api/bookings/requests")
                                .param("ownerId", "70"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id", is(1)));
        }
}
