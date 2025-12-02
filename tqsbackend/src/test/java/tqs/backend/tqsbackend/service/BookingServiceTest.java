package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.PaymentStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.exception.AvailabilityException;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.exception.PaymentException;
import tqs.backend.tqsbackend.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingService bookingService;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item();
        sampleItem.setId(10L);
        sampleItem.setOwnerId(20L);
        sampleItem.setPrice(55.0);
    }

    @Test
    @DisplayName("When payment succeeds the booking is confirmed and paid")
    void createBooking_paymentSuccess_confirmsBooking() {
        BookingCreateRequest request = buildRequest();

        when(itemService.getItemById(request.getItemId())).thenReturn(sampleItem);
        when(bookingRepository.existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(anyLong(), any(), any(), any()))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking persisted = inv.getArgument(0);
            if (persisted.getId() == null) {
                persisted.setId(42L);
            }
            return persisted;
        });
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        when(paymentService.charge(eq(request.getRenterId()), eq(sampleItem), any(BigDecimal.class), eq(42L)))
                .thenReturn(PaymentResult.success("PAY-42"));

        Booking result = bookingService.createBooking(request);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getPaymentReference()).isEqualTo("PAY-42");
        assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("165.00"));
        verify(paymentService).charge(eq(request.getRenterId()), eq(sampleItem), amountCaptor.capture(), eq(42L));
        assertThat(amountCaptor.getValue()).isEqualByComparingTo(new BigDecimal("165.00"));
        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Payment failure marks booking rejected and raises exception")
    void createBooking_paymentFailure_rejectsBooking() {
        BookingCreateRequest request = buildRequest();

        when(itemService.getItemById(request.getItemId())).thenReturn(sampleItem);
        when(bookingRepository.existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(anyLong(), any(), any(), any()))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking persisted = inv.getArgument(0);
            if (persisted.getId() == null) {
                persisted.setId(100L);
            }
            return persisted;
        });
        when(paymentService.charge(anyLong(), any(Item.class), any(BigDecimal.class), anyLong()))
                .thenReturn(PaymentResult.failure("card declined"));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("card declined");

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(2)).save(captor.capture());
        Booking rejected = captor.getAllValues().get(1);
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(rejected.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Overlapping booking triggers availability exception")
    void createBooking_overlappingDates_throws() {
        BookingCreateRequest request = buildRequest();

        when(itemService.getItemById(request.getItemId())).thenReturn(sampleItem);
        when(bookingRepository.existsByItemIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(anyLong(), any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(AvailabilityException.class);

        verify(bookingRepository, never()).save(any());
        verify(paymentService, never()).charge(anyLong(), any(Item.class), any(BigDecimal.class), anyLong());
    }

    @Nested
    class Validation {

        @Test
        void startDateInPast_throwsValidation() {
            BookingCreateRequest request = buildRequest();
            request.setStartDate(LocalDate.now().minusDays(1));

            when(itemService.getItemById(request.getItemId())).thenReturn(sampleItem);

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BookingValidationException.class)
                    .hasMessageContaining("Start date");
        }

        @Test
        void endDateNotAfterStart_throwsValidation() {
            BookingCreateRequest request = buildRequest();
            request.setEndDate(request.getStartDate());

            when(itemService.getItemById(request.getItemId())).thenReturn(sampleItem);

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BookingValidationException.class)
                    .hasMessageContaining("after start date");
        }

        @Test
        void renterMissing_throwsValidation() {
            BookingCreateRequest request = buildRequest();
            request.setRenterId(null);

            when(itemService.getItemById(request.getItemId())).thenReturn(sampleItem);

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BookingValidationException.class)
                    .hasMessageContaining("Renter");
        }

        @Test
        void renterIsOwner_throwsValidation() {
            BookingCreateRequest request = buildRequest();
            sampleItem.setOwnerId(request.getRenterId());

            when(itemService.getItemById(request.getItemId())).thenReturn(sampleItem);

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BookingValidationException.class)
                    .hasMessageContaining("own item");
        }

        @Test
        void itemMissing_throwsValidation() {
            BookingCreateRequest request = buildRequest();

            when(itemService.getItemById(request.getItemId())).thenReturn(null);

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BookingValidationException.class)
                    .hasMessageContaining("Item not found");
        }
    }

    private BookingCreateRequest buildRequest() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(10L);
        request.setRenterId(70L);
        request.setStartDate(LocalDate.now().plusDays(3));
        request.setEndDate(LocalDate.now().plusDays(6));
        return request;
    }
}
