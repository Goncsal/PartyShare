package tqs.backend.tqsbackend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BookingResponseTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        BookingResponse response = new BookingResponse();
        response.setId(1L);
        response.setItemId(10L);
        response.setRenterId(20L);
        response.setStartDate(LocalDate.of(2024, 1, 1));
        response.setEndDate(LocalDate.of(2024, 1, 5));
        response.setTotalPrice(new BigDecimal("100.00"));
        response.setStatus(BookingStatus.ACCEPTED);
        response.setPaymentStatus(PaymentStatus.PAID);
        response.setPaymentReference("REF-123");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getItemId()).isEqualTo(10L);
        assertThat(response.getRenterId()).isEqualTo(20L);
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 5));
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.getPaymentReference()).isEqualTo("REF-123");
    }

    @Test
    void testAllArgsConstructor() {
        BookingResponse response = new BookingResponse(
                1L, 10L, 20L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 5),
                new BigDecimal("100.00"),
                BookingStatus.REQUESTED,
                PaymentStatus.PENDING,
                null
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getItemId()).isEqualTo(10L);
        assertThat(response.getRenterId()).isEqualTo(20L);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void testStatusValues() {
        BookingResponse response = new BookingResponse();
        
        response.setStatus(BookingStatus.REQUESTED);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.REQUESTED);
        
        response.setStatus(BookingStatus.ACCEPTED);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        
        response.setStatus(BookingStatus.REJECTED);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.REJECTED);
        
        response.setStatus(BookingStatus.CANCELLED);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void testPaymentStatusValues() {
        BookingResponse response = new BookingResponse();
        
        response.setPaymentStatus(PaymentStatus.PENDING);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        
        response.setPaymentStatus(PaymentStatus.PAID);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }
}
