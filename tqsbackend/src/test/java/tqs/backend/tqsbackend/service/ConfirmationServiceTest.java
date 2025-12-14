package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.repository.BookingRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private ConfirmationService confirmationService;

    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setOwnerId(1L);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setRenterId(2L);
        booking.setTotalPrice(new BigDecimal("100.00"));
        booking.setRenterConfirmed(false);
        booking.setOwnerConfirmed(false);
    }

    @Test
    void confirmByRenter_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        boolean result = confirmationService.confirmByRenter(1L, 2L);

        assertThat(result).isTrue();
        assertThat(booking.isRenterConfirmed()).isTrue();
        assertThat(booking.getReturnedAt()).isNotNull();
        verify(bookingRepository).save(booking);
    }

    @Test
    void confirmByRenter_WrongUser_ReturnsFalse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean result = confirmationService.confirmByRenter(1L, 99L);

        assertThat(result).isFalse();
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmByRenter_BookingNotFound_ReturnsFalse() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = confirmationService.confirmByRenter(99L, 2L);

        assertThat(result).isFalse();
    }

    @Test
    void confirmByOwner_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        boolean result = confirmationService.confirmByOwner(1L, 1L);

        assertThat(result).isTrue();
        assertThat(booking.isOwnerConfirmed()).isTrue();
        verify(bookingRepository).save(booking);
    }

    @Test
    void confirmByOwner_WrongUser_ReturnsFalse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean result = confirmationService.confirmByOwner(1L, 99L);

        assertThat(result).isFalse();
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void dualConfirmation_TriggersRelease() {
        booking.setRenterConfirmed(true);
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(walletService.releaseFunds(1L)).thenReturn(true);

        boolean result = confirmationService.confirmByOwner(1L, 1L);

        assertThat(result).isTrue();
        assertThat(booking.isOwnerConfirmed()).isTrue();
        verify(walletService).releaseFunds(1L);
    }

    @Test
    void isFullyConfirmed_True() {
        booking.setRenterConfirmed(true);
        booking.setOwnerConfirmed(true);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean result = confirmationService.isFullyConfirmed(1L);

        assertThat(result).isTrue();
    }

    @Test
    void isFullyConfirmed_False() {
        booking.setRenterConfirmed(true);
        booking.setOwnerConfirmed(false);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean result = confirmationService.isFullyConfirmed(1L);

        assertThat(result).isFalse();
    }

    @Test
    void isFullyConfirmed_BookingNotFound_ReturnsFalse() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = confirmationService.isFullyConfirmed(99L);

        assertThat(result).isFalse();
    }

    @Test
    void confirmByOwner_BookingNotFound_ReturnsFalse() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = confirmationService.confirmByOwner(99L, 1L);

        assertThat(result).isFalse();
    }

    @Test
    void confirmByRenter_AlreadyConfirmed_ReturnsTrue() {
        booking.setRenterConfirmed(true);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean result = confirmationService.confirmByRenter(1L, 2L);

        assertThat(result).isTrue();
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmByOwner_AlreadyConfirmed_ReturnsTrue() {
        booking.setOwnerConfirmed(true);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean result = confirmationService.confirmByOwner(1L, 1L);

        assertThat(result).isTrue();
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmByRenter_WithExistingReturnedAt_DoesNotOverwrite() {
        java.time.LocalDateTime existingTime = java.time.LocalDateTime.now().minusDays(1);
        booking.setReturnedAt(existingTime);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        boolean result = confirmationService.confirmByRenter(1L, 2L);

        assertThat(result).isTrue();
        assertThat(booking.getReturnedAt()).isEqualTo(existingTime);
    }

    @Test
    void dualConfirmation_RenterFirst_ThenOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(walletService.releaseFunds(1L)).thenReturn(true);

        // Renter confirms first
        boolean renterResult = confirmationService.confirmByRenter(1L, 2L);
        assertThat(renterResult).isTrue();

        // Owner confirms second - should trigger release
        boolean ownerResult = confirmationService.confirmByOwner(1L, 1L);
        assertThat(ownerResult).isTrue();
        verify(walletService).releaseFunds(1L);
    }
}
