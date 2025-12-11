package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.backend.tqsbackend.dto.DateRangeDto;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceUnavailableDatesTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void getUnavailableDates_WithActiveBookings_ReturnsDateRanges() {
        Item item = new Item();
        item.setId(1L);

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setItem(item);
        booking1.setStartDate(LocalDate.of(2024, 1, 10));
        booking1.setEndDate(LocalDate.of(2024, 1, 15));
        booking1.setStatus(BookingStatus.CONFIRMED);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setItem(item);
        booking2.setStartDate(LocalDate.of(2024, 1, 20));
        booking2.setEndDate(LocalDate.of(2024, 1, 25));
        booking2.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findActiveBookingsByItemId(1L))
                .thenReturn(Arrays.asList(booking1, booking2));

        List<DateRangeDto> result = bookingService.getUnavailableDates(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStartDate()).isEqualTo("2024-01-10");
        assertThat(result.get(0).getEndDate()).isEqualTo("2024-01-15");
        assertThat(result.get(1).getStartDate()).isEqualTo("2024-01-20");
        assertThat(result.get(1).getEndDate()).isEqualTo("2024-01-25");
    }

    @Test
    void getUnavailableDates_WithNoBookings_ReturnsEmptyList() {
        when(bookingRepository.findActiveBookingsByItemId(1L))
                .thenReturn(Arrays.asList());

        List<DateRangeDto> result = bookingService.getUnavailableDates(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getUnavailableDates_WithSingleBooking_ReturnsSingleDateRange() {
        Item item = new Item();
        item.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setStartDate(LocalDate.of(2024, 6, 1));
        booking.setEndDate(LocalDate.of(2024, 6, 5));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findActiveBookingsByItemId(1L))
                .thenReturn(Arrays.asList(booking));

        List<DateRangeDto> result = bookingService.getUnavailableDates(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartDate()).isEqualTo("2024-06-01");
        assertThat(result.get(0).getEndDate()).isEqualTo("2024-06-05");
    }
}
