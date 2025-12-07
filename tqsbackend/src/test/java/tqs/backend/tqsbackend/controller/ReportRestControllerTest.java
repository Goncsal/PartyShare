package tqs.backend.tqsbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Report;
import tqs.backend.tqsbackend.entity.ReportState;
import tqs.backend.tqsbackend.service.ReportService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportRestController.class)
class ReportRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    private Report validReport;

    @BeforeEach
    void setUp() {
        validReport = new Report(1L, "Bug Found", "Description");
        validReport.setId(10L);
    }

    @Test
    void createReport_success() throws Exception {
        when(reportService.createReport(eq(1L), eq("Bug Found"), eq("Description"))).thenReturn(validReport);

        mockMvc.perform(post("/api/reports/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Bug Found"));
    }

    @Test
    void createReport_invalid() throws Exception {
        when(reportService.createReport(eq(1L), eq(""), any())).thenThrow(new IllegalArgumentException());

        Report invalidReport = new Report(1L, "", "Desc");

        mockMvc.perform(post("/api/reports/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReport)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReportById_found() throws Exception {
        when(reportService.getReportById(10L)).thenReturn(Optional.of(validReport));

        mockMvc.perform(get("/api/reports/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getReportById_notFound() throws Exception {
        when(reportService.getReportById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reports/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReportState_success() throws Exception {
        when(reportService.updateReportState(10L)).thenReturn(true);

        mockMvc.perform(put("/api/reports/10/state"))
                .andExpect(status().isOk());
    }

    @Test
    void updateReportState_notFound() throws Exception {
        when(reportService.updateReportState(99L)).thenReturn(false);

        mockMvc.perform(put("/api/reports/99/state"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReportsBySender_success() throws Exception {
        when(reportService.getReportsBySenderId(1L)).thenReturn(List.of(validReport));

        mockMvc.perform(get("/api/reports/sender/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    void getReportsByState_success() throws Exception {
        when(reportService.getReportsByState(ReportState.NEW)).thenReturn(List.of(validReport));

        mockMvc.perform(get("/api/reports/state/NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }
}
