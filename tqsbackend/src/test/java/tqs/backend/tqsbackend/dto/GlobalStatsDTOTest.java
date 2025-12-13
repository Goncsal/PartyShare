package tqs.backend.tqsbackend.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalStatsDTOTest {

    @Test
    void testBuilder() {
        GlobalStatsDTO dto = GlobalStatsDTO.builder()
                .totalUsers(100)
                .totalBookings(50)
                .totalRevenue(5000.0)
                .pendingReports(5)
                .averageUserRating(4.5)
                .build();

        assertThat(dto.getTotalUsers()).isEqualTo(100);
        assertThat(dto.getTotalBookings()).isEqualTo(50);
        assertThat(dto.getTotalRevenue()).isEqualTo(5000.0);
        assertThat(dto.getPendingReports()).isEqualTo(5);
        assertThat(dto.getAverageUserRating()).isEqualTo(4.5);
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        GlobalStatsDTO dto = new GlobalStatsDTO();
        dto.setTotalUsers(200);
        dto.setTotalBookings(100);
        dto.setTotalRevenue(10000.0);
        dto.setPendingReports(10);
        dto.setAverageUserRating(3.8);

        assertThat(dto.getTotalUsers()).isEqualTo(200);
        assertThat(dto.getTotalBookings()).isEqualTo(100);
        assertThat(dto.getTotalRevenue()).isEqualTo(10000.0);
        assertThat(dto.getPendingReports()).isEqualTo(10);
        assertThat(dto.getAverageUserRating()).isEqualTo(3.8);
    }

    @Test
    void testAllArgsConstructor() {
        GlobalStatsDTO dto = new GlobalStatsDTO(150, 75, 7500.0, 8, 4.2);

        assertThat(dto.getTotalUsers()).isEqualTo(150);
        assertThat(dto.getTotalBookings()).isEqualTo(75);
        assertThat(dto.getTotalRevenue()).isEqualTo(7500.0);
        assertThat(dto.getPendingReports()).isEqualTo(8);
        assertThat(dto.getAverageUserRating()).isEqualTo(4.2);
    }

    @Test
    void testEqualsAndHashCode() {
        GlobalStatsDTO dto1 = new GlobalStatsDTO(100, 50, 5000.0, 5, 4.5);
        GlobalStatsDTO dto2 = new GlobalStatsDTO(100, 50, 5000.0, 5, 4.5);
        GlobalStatsDTO dto3 = new GlobalStatsDTO(200, 50, 5000.0, 5, 4.5);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1).isNotEqualTo(dto3);
    }

    @Test
    void testToString() {
        GlobalStatsDTO dto = new GlobalStatsDTO(100, 50, 5000.0, 5, 4.5);
        String toString = dto.toString();

        assertThat(toString).contains("totalUsers=100");
        assertThat(toString).contains("totalBookings=50");
        assertThat(toString).contains("totalRevenue=5000.0");
        assertThat(toString).contains("pendingReports=5");
        assertThat(toString).contains("averageUserRating=4.5");
    }
}
