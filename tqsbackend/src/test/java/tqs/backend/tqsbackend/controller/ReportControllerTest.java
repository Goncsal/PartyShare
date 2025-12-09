package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.service.ReportService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void listReports_notLoggedIn_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void listReports_loggedIn_showsReports() throws Exception {
        Long userId = 1L;
        List<Report> reports = List.of(new Report(userId, "Title", "Desc"));

        when(reportService.getReportsBySenderId(userId)).thenReturn(reports);

        mockMvc.perform(get("/reports")
                .sessionAttr("userId", userId)
                .sessionAttr("userName", "TestUser"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attribute("reports", reports))
                .andExpect(model().attribute("isLoggedIn", true))
                .andExpect(model().attribute("userId", userId))
                .andExpect(model().attribute("userName", "TestUser"));
    }

    @Test
    void createReport_notLoggedIn_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/reports/new")
                .param("title", "Bug")
                .param("description", "Desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void createReport_loggedIn_success() throws Exception {
        Long userId = 1L;
        String title = "Bug";
        String description = "Desc";

        mockMvc.perform(post("/reports/new")
                .sessionAttr("userId", userId)
                .param("title", title)
                .param("description", description))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"))
                .andExpect(flash().attribute("success", "Report submitted successfully!"));

        verify(reportService).createReport(userId, title, description);
    }

    @Test
    void createReport_loggedIn_failure() throws Exception {
        Long userId = 1L;
        String title = "";
        String description = "Desc";

        doThrow(new IllegalArgumentException("Invalid Title"))
                .when(reportService).createReport(userId, title, description);

        mockMvc.perform(post("/reports/new")
                .sessionAttr("userId", userId)
                .param("title", title)
                .param("description", description))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"))
                .andExpect(flash().attribute("error", "Failed to submit report: Invalid Title"));
    }
}
