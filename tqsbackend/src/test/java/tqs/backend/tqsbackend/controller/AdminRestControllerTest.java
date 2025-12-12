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

    @Test
    void getDashboardStats_ReturnsStats() throws Exception {
        given(categoryService.getAllCategories()).willReturn(Collections.emptyList());

        mvc.perform(get("/api/admin/dashboard-stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryCount", is(0)));
    }
}
