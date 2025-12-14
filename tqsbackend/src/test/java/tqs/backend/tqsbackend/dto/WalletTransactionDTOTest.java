package tqs.backend.tqsbackend.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTransactionDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        WalletTransactionDTO dto = new WalletTransactionDTO();
        dto.setId(1L);
        dto.setBookingId(10L);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setStatus("PENDING");
        dto.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));
        dto.setReleasedAt(null);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBookingId()).isEqualTo(10L);
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getStatus()).isEqualTo("PENDING");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
        assertThat(dto.getReleasedAt()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime releasedAt = LocalDateTime.now().plusDays(1);
        WalletTransactionDTO dto = new WalletTransactionDTO(1L, 10L, new BigDecimal("100.00"), "RELEASED", createdAt, releasedAt);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBookingId()).isEqualTo(10L);
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getStatus()).isEqualTo("RELEASED");
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getReleasedAt()).isEqualTo(releasedAt);
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime createdAt = LocalDateTime.now();
        WalletTransactionDTO dto1 = new WalletTransactionDTO(1L, 10L, new BigDecimal("100.00"), "PENDING", createdAt, null);
        WalletTransactionDTO dto2 = new WalletTransactionDTO(1L, 10L, new BigDecimal("100.00"), "PENDING", createdAt, null);
        WalletTransactionDTO dto3 = new WalletTransactionDTO(2L, 20L, new BigDecimal("200.00"), "RELEASED", createdAt, null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void testToString() {
        WalletTransactionDTO dto = new WalletTransactionDTO(1L, 10L, new BigDecimal("100.00"), "PENDING", LocalDateTime.now(), null);
        String result = dto.toString();
        
        assertThat(result).contains("WalletTransactionDTO");
        assertThat(result).contains("id=1");
        assertThat(result).contains("PENDING");
    }
}
