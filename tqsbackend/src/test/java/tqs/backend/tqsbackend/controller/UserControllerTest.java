package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tqs.backend.tqsbackend.dto.UserRegistrationDto;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.UserService;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @Test
    void login_View() throws Exception {
        mvc.perform(get("/users/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"));
    }

    @Test
    void login_Post_Success() throws Exception {
        User user = new User("John", "john@ua.pt", "password", UserRoles.RENTER);
        user.setId(1L);

        when(userService.authenticate("john@ua.pt", "password")).thenReturn(true);
        when(userService.getUserByEmail("john@ua.pt")).thenReturn(java.util.Optional.of(user));

        mvc.perform(post("/users/login")
                .param("email", "john@ua.pt")
                .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"))
                .andExpect(flash().attribute("success", "Login successful!"));
    }

    @Test
    void login_Post_Failure() throws Exception {
        when(userService.authenticate("john@ua.pt", "wrong")).thenReturn(false);

        mvc.perform(post("/users/login")
                .param("email", "john@ua.pt")
                .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"))
                .andExpect(model().attribute("error", "Invalid credentials"));
    }

    @Test
    void register_View() throws Exception {
        mvc.perform(get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", instanceOf(UserRegistrationDto.class)));
    }

    @Test
    void register_Post_Success() throws Exception {
        mvc.perform(post("/users/register")
                .param("name", "John")
                .param("email", "john@ua.pt")
                .param("password", "password")
                .param("role", "RENTER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"))
                .andExpect(flash().attribute("success", "Registration successful! Please login."));

        verify(userService).registerUser("John", "john@ua.pt", "password", UserRoles.RENTER);
    }

    @Test
    void register_Post_Failure() throws Exception {
        doThrow(new IllegalArgumentException("Invalid Email")).when(userService).registerUser(any(), any(), any(),
                any());

        mvc.perform(post("/users/register")
                .param("name", "John")
                .param("email", "invalid")
                .param("password", "password")
                .param("role", "RENTER"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"))
                .andExpect(model().attribute("error", "Invalid Email"));
    }

    @Test
    void register_Post_DuplicateEmail() throws Exception {
        doThrow(new IllegalArgumentException("Failed to register user: Email already exists."))
                .when(userService).registerUser("John", "duplicate@ua.pt", "password123", UserRoles.RENTER);

        mvc.perform(post("/users/register")
                .param("name", "John")
                .param("email", "duplicate@ua.pt")
                .param("password", "password123")
                .param("role", "RENTER"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/register"))
                .andExpect(model().attribute("error", "Failed to register user: Email already exists."));
    }

    @Test
    void profile_View_LoggedIn() throws Exception {
        mvc.perform(get("/users/profile").sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("users/profile"));
    }

    @Test
    void profile_View_LoggedOut() throws Exception {
        mvc.perform(get("/users/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }
}