package tqs.backend.tqsbackend.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WalletDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        WalletDTO dto = new WalletDTO();
        dto.setId(1L);
        dto.setBalance(new BigDecimal("500.00"));
        dto.setPendingBalance(new BigDecimal("100.00"));
        dto.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(dto.getPendingBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now();
        WalletDTO dto = new WalletDTO(1L, new BigDecimal("500.00"), new BigDecimal("100.00"), createdAt);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(dto.getPendingBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime createdAt = LocalDateTime.now();
        WalletDTO dto1 = new WalletDTO(1L, new BigDecimal("500.00"), new BigDecimal("100.00"), createdAt);
        WalletDTO dto2 = new WalletDTO(1L, new BigDecimal("500.00"), new BigDecimal("100.00"), createdAt);
        WalletDTO dto3 = new WalletDTO(2L, new BigDecimal("200.00"), new BigDecimal("50.00"), createdAt);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void testToString() {
        WalletDTO dto = new WalletDTO(1L, new BigDecimal("500.00"), new BigDecimal("100.00"), LocalDateTime.now());
        String result = dto.toString();
        
        assertThat(result).contains("WalletDTO");
        assertThat(result).contains("id=1");
        assertThat(result).contains("500.00");
    }
}
