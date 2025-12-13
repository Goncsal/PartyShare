package tqs.backend.tqsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatsDTO {
    private long totalUsers;
    private long totalBookings;
    private double totalRevenue;
    private long pendingReports;
    private double averageUserRating;
}
