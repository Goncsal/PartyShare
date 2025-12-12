package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private tqs.backend.tqsbackend.service.CategoryService categoryService;

    @MockitoBean
    private tqs.backend.tqsbackend.service.ReportService reportService;

    @Test
    void whenGetUsers_thenReturnUsersPage() throws Exception {
        User user = new User("John", "john@ua.pt", "pass", UserRoles.RENTER);
        when(userService.searchUsers(any(), any(), any(), any())).thenReturn(List.of(user));

        mvc.perform(get("/admin/users")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void whenGetUsersWithFilters_thenPassFiltersToService() throws Exception {
        User user = new User("John", "john@ua.pt", "pass", UserRoles.RENTER);
        when(userService.searchUsers(eq("John"), eq(UserRoles.RENTER), any(), any())).thenReturn(List.of(user));

        mvc.perform(get("/admin/users")
                .param("keyword", "John")
                .param("role", "RENTER")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void whenGetUsersAsNonAdmin_thenRedirect() throws Exception {
        mvc.perform(get("/admin/users")
                .sessionAttr("userRole", UserRoles.RENTER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void whenGetDashboard_thenReturnDashboardPage() throws Exception {
        mvc.perform(get("/admin/dashboard")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    void whenGetDashboardAsNonAdmin_thenRedirect() throws Exception {
        mvc.perform(get("/admin/dashboard")
                .sessionAttr("userRole", UserRoles.RENTER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void whenGetCategories_thenReturnCategoriesPage() throws Exception {
        mvc.perform(get("/admin/categories")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void whenGetCategoriesAsNonAdmin_thenRedirect() throws Exception {
        mvc.perform(get("/admin/categories")
                .sessionAttr("userRole", UserRoles.RENTER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void whenCreateCategory_thenRedirectToCategories() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin/categories")
                .param("name", "New Category")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void whenCreateCategoryAsNonAdmin_thenRedirect() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin/categories")
                .param("name", "New Category")
                .sessionAttr("userRole", UserRoles.RENTER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void whenGetReports_thenReturnReportsPage() throws Exception {
        mvc.perform(get("/admin/reports")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reports"))
                .andExpect(model().attributeExists("reports"));
    }

    @Test
    void whenGetReportsWithFilter_thenReturnFilteredReports() throws Exception {
        mvc.perform(get("/admin/reports")
                .param("state", "NEW")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reports"))
                .andExpect(model().attributeExists("reports"));
        
        // Verify that service was called with the correct state
        org.mockito.Mockito.verify(reportService).searchReports(tqs.backend.tqsbackend.entity.ReportState.NEW);
    }

    @Test
    void whenGetReportsAsNonAdmin_thenRedirect() throws Exception {
        mvc.perform(get("/admin/reports")
                .sessionAttr("userRole", UserRoles.RENTER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void whenDeactivateUser_thenRedirectToProfile() throws Exception {
        when(userService.deactivateUser(1L)).thenReturn(true);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin/users/1/deactivate")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void whenActivateUser_thenRedirectToProfile() throws Exception {
        when(userService.activateUser(1L)).thenReturn(true);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin/users/1/activate")
                .sessionAttr("userRole", UserRoles.ADMIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void whenDeactivateUserAsNonAdmin_thenRedirect() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin/users/1/deactivate")
                .sessionAttr("userRole", UserRoles.RENTER))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }
}
