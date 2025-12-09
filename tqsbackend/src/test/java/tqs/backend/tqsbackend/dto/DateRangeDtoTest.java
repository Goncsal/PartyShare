package tqs.backend.tqsbackend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DateRangeDtoTest {

    @Test
    void constructor_SetsValuesCorrectly() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 5);
        DateRangeDto dto = new DateRangeDto(start, end);
        
        assertThat(dto.getStartDate()).isEqualTo(start);
        assertThat(dto.getEndDate()).isEqualTo(end);
    }

    @Test
    void dateRangeDto_WithDifferentDates() {
        LocalDate start1 = LocalDate.of(2024, 3, 15);
        LocalDate end1 = LocalDate.of(2024, 3, 20);
        LocalDate start2 = LocalDate.of(2024, 6, 1);
        LocalDate end2 = LocalDate.of(2024, 6, 30);
        
        DateRangeDto dto1 = new DateRangeDto(start1, end1);
        DateRangeDto dto2 = new DateRangeDto(start2, end2);
        
        assertThat(dto1.getStartDate()).isNotEqualTo(dto2.getStartDate());
        assertThat(dto1.getEndDate()).isNotEqualTo(dto2.getEndDate());
    }

    @Test
    void dateRangeDto_WithSameDay() {
        LocalDate sameDay = LocalDate.of(2024, 5, 15);
        DateRangeDto dto = new DateRangeDto(sameDay, sameDay);
        
        assertThat(dto.getStartDate()).isEqualTo(dto.getEndDate());
    }
}
