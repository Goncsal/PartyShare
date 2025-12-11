package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.fixtures.BookingTestFixtures;
import tqs.backend.tqsbackend.repository.BookingRepository;

/**
 * TDD tests for Cancel Rental functionality (US 1.7)
 */
@ExtendWith(MockitoExtension.class)
class IT_CancelRentalServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingService bookingService;

    private Item sampleItem;
    private Booking confirmedBooking;
    private Booking pendingBooking;

    @BeforeEach
    void setUp() {
        sampleItem = BookingTestFixtures.sampleItem(10L);
        confirmedBooking = BookingTestFixtures.sampleBooking(1L, sampleItem);
        confirmedBooking.setStatus(BookingStatus.ACCEPTED);
        
        pendingBooking = BookingTestFixtures.sampleBooking(2L, sampleItem);
        pendingBooking.setStatus(BookingStatus.REQUESTED);
    }

    @Test
    @DisplayName("Cancel booking succeeds when status is CONFIRMED")
    void cancelBooking_success_whenConfirmed() {
        Long bookingId = 1L;
        Long renterId = confirmedBooking.getRenterId();
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(confirmedBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.cancelBooking(bookingId, renterId);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(confirmedBooking);
    }

    @Test
    @DisplayName("Cancel booking succeeds when status is PENDING")
    void cancelBooking_success_whenPending() {
        Long bookingId = 2L;
        Long renterId = pendingBooking.getRenterId();
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.cancelBooking(bookingId, renterId);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(pendingBooking);
    }

    @Test
    @DisplayName("Cancel booking fails when booking not found")
    void cancelBooking_fails_whenBookingNotFound() {
        Long bookingId = 999L;
        Long renterId = 70L;
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, renterId))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("Booking not found");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancel booking fails when renter is not the owner of booking")
    void cancelBooking_fails_whenNotOwner() {
        Long bookingId = 1L;
        Long wrongRenterId = 999L;
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(confirmedBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, wrongRenterId))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("cancel your own bookings");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancel booking fails when booking already cancelled")
    void cancelBooking_fails_whenAlreadyCancelled() {
        Long bookingId = 1L;
        Long renterId = confirmedBooking.getRenterId();
        confirmedBooking.setStatus(BookingStatus.CANCELLED);
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(confirmedBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, renterId))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("cannot be cancelled");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancel booking fails when booking was rejected")
    void cancelBooking_fails_whenRejected() {
        Long bookingId = 1L;
        Long renterId = confirmedBooking.getRenterId();
        confirmedBooking.setStatus(BookingStatus.REJECTED);
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(confirmedBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, renterId))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("cannot be cancelled");

        verify(bookingRepository, never()).save(any());
    }
}
