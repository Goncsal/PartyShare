package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.service.BookingService;
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.UserService;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRestController.class)
class AdminRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    // @MockBean
    // private ItemService itemService;

    @MockBean
    private tqs.backend.tqsbackend.service.ReportService reportService;

    @Test
    void getDashboardStats_ReturnsStats() throws Exception {
        given(categoryService.getAllCategories()).willReturn(Collections.emptyList());

        mvc.perform(get("/api/admin/dashboard-stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryCount", is(0)));
    }

    @Test
    void getAllReports_ReturnsReports() throws Exception {
        given(reportService.searchReports(null)).willReturn(Collections.emptyList());

        mvc.perform(get("/api/admin/reports")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(Collections.emptyList())));
    }

    @Test
    void getAllReports_WithFilter_ReturnsFilteredReports() throws Exception {
        given(reportService.searchReports(tqs.backend.tqsbackend.entity.ReportState.NEW)).willReturn(Collections.emptyList());

        mvc.perform(get("/api/admin/reports")
                .param("state", "NEW")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(Collections.emptyList())));
        
        org.mockito.Mockito.verify(reportService).searchReports(tqs.backend.tqsbackend.entity.ReportState.NEW);
    }

    @Test
    void activateUser_ValidId_ReturnsOk() throws Exception {
        given(userService.activateUser(1L)).willReturn(true);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/1/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void activateUser_InvalidId_ReturnsBadRequest() throws Exception {
        given(userService.activateUser(1L)).willReturn(false);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/1/activate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateUser_ValidId_ReturnsOk() throws Exception {
        given(userService.deactivateUser(1L)).willReturn(true);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/1/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateUser_InvalidId_ReturnsBadRequest() throws Exception {
        given(userService.deactivateUser(1L)).willReturn(false);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users/1/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
