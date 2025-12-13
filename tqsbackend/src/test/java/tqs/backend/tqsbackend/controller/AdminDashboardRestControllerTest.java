package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.dto.GlobalStatsDTO;
import tqs.backend.tqsbackend.service.AdminDashboardService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDashboardRestController.class)
class AdminDashboardRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService adminDashboardService;

    @Test
    void getGlobalStats_shouldReturnStatsJson() throws Exception {
        GlobalStatsDTO stats = GlobalStatsDTO.builder()
                .totalUsers(100)
                .totalBookings(500)
                .totalRevenue(1000.50)
                .pendingReports(3)
                .averageUserRating(4.2)
                .build();

        when(adminDashboardService.getGlobalStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.totalBookings").value(500))
                .andExpect(jsonPath("$.totalRevenue").value(1000.50))
                .andExpect(jsonPath("$.pendingReports").value(3))
                .andExpect(jsonPath("$.averageUserRating").value(4.2));
    }
}
