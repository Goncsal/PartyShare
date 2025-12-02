package tqs.backend.tqsbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
class UserRestControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private UserService userService;

        private final ObjectMapper mapper = new ObjectMapper();

        @Test
        void registerUser_Success() throws Exception {
                User saved = new User("John", "john@ua.pt", "hash", UserRoles.RENTER);
                saved.setId(1L);

                when(userService.registerUser(any(), any(), any(), any())).thenReturn(saved);

                Map<String, Object> request = new HashMap<>();
                request.put("name", "John");
                request.put("email", "john@ua.pt");
                request.put("password", "password1");
                request.put("role", "RENTER");

                mvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.email").value("john@ua.pt"));
        }

        @Test
        void registerUser_Invalid() throws Exception {
                when(userService.registerUser(any(), any(), any(), any()))
                                .thenThrow(new IllegalArgumentException("Invalid Email"));

                Map<String, Object> request = new HashMap<>();
                request.put("name", "John");
                request.put("email", "");
                request.put("password", "password1");
                request.put("role", "RENTER");

                mvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Invalid Email"));
        }

        @Test
        void login_Success() throws Exception {
                when(userService.authenticate("john@ua.pt", "pass")).thenReturn(true);

                Map<String, String> creds = new HashMap<>();
                creds.put("email", "john@ua.pt");
                creds.put("password", "pass");

                mvc.perform(post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(creds)))
                                .andExpect(status().isOk());
        }

        @Test
        void getUserById_Found() throws Exception {
                User u = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUserById(1L)).thenReturn(Optional.of(u));

                mvc.perform(get("/api/users/id/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("John"));
        }

        @Test
        void login_Failure() throws Exception {
                when(userService.authenticate("john@ua.pt", "wrongpass")).thenReturn(false);

                Map<String, String> creds = new HashMap<>();
                creds.put("email", "john@ua.pt");
                creds.put("password", "wrongpass");

                mvc.perform(post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(creds)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$").value("Invalid credentials"));
        }

        @Test
        void getUserById_NotFound() throws Exception {
                when(userService.getUserById(99L)).thenReturn(Optional.empty());

                mvc.perform(get("/api/users/id/99"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getUserByEmail_Found() throws Exception {
                User u = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUserByEmail("j@ua.pt")).thenReturn(Optional.of(u));

                mvc.perform(get("/api/users/email/j@ua.pt"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("John"));
        }

        @Test
        void getUserByEmail_NotFound() throws Exception {
                when(userService.getUserByEmail("unknown@ua.pt")).thenReturn(Optional.empty());

                mvc.perform(get("/api/users/email/unknown@ua.pt"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void searchUsers_All() throws Exception {
                User u1 = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                User u2 = new User("Jane", "jane@ua.pt", "h", UserRoles.OWNER);
                when(userService.getAllUsers()).thenReturn(java.util.List.of(u1, u2));

                mvc.perform(get("/api/users/search"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void searchUsers_ByName() throws Exception {
                User u1 = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUsersByName("John")).thenReturn(java.util.List.of(u1));

                mvc.perform(get("/api/users/search").param("name", "John"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].name").value("John"));
        }

        @Test
        void searchUsers_ByRole() throws Exception {
                User u2 = new User("Jane", "jane@ua.pt", "h", UserRoles.OWNER);
                when(userService.getUsersByRole(UserRoles.OWNER)).thenReturn(java.util.List.of(u2));

                mvc.perform(get("/api/users/search").param("role", "OWNER"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].role").value("OWNER"));
        }

        @Test
        void searchUsers_ByStatus() throws Exception {
                User u1 = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUsersByStatus(true)).thenReturn(java.util.List.of(u1));

                mvc.perform(get("/api/users/search").param("active", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void searchUsers_ByNameAndRole() throws Exception {
                User u1 = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUsersByNameAndRole("John", UserRoles.RENTER)).thenReturn(java.util.List.of(u1));

                mvc.perform(get("/api/users/search")
                                .param("name", "John")
                                .param("role", "RENTER"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void searchUsers_ByNameAndStatus() throws Exception {
                User u1 = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUsersByNameAndStatus("John", true)).thenReturn(java.util.List.of(u1));

                mvc.perform(get("/api/users/search")
                                .param("name", "John")
                                .param("active", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void searchUsers_ByRoleAndStatus() throws Exception {
                User u1 = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUsersByRoleAndStatus(UserRoles.RENTER, true)).thenReturn(java.util.List.of(u1));

                mvc.perform(get("/api/users/search")
                                .param("role", "RENTER")
                                .param("active", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void searchUsers_ByNameAndRoleAndStatus() throws Exception {
                User u1 = new User("John", "j@ua.pt", "h", UserRoles.RENTER);
                when(userService.getUsersByNameAndRoleAndStatus("John", UserRoles.RENTER, true))
                                .thenReturn(java.util.List.of(u1));

                mvc.perform(get("/api/users/search")
                                .param("name", "John")
                                .param("role", "RENTER")
                                .param("active", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1));
        }
}
