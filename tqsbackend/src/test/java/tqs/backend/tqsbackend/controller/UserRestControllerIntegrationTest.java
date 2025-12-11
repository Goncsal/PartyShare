package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.dto.UserLoginRequest;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class UserRestControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void resetDb() {
        userRepository.deleteAll();
    }

    @Test
    void whenValidInput_thenCreateUser() throws Exception {
        String body = """
                {
                    "name": "testuser",
                    "email": "test@ua.pt",
                    "password": "password",
                    "role": "RENTER"
                }
                """;

        mvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());

        assertThat(userRepository.findAll()).hasSize(4); // 3 seeded users + 1 new user
    }

    @Test
    void givenUser_whenGetUsers_thenStatus200() throws Exception {
        User user = new User("John", "john@ua.pt", "pass", UserRoles.RENTER);
        userRepository.save(user);

        mvc.perform(get("/api/users/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void givenUser_whenLogin_thenStatus200() throws Exception {
        String password = "password123";
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        User user = new User("John", "john@ua.pt", hashedPassword, UserRoles.RENTER);
        userRepository.save(user);

        UserLoginRequest loginRequest = new UserLoginRequest("john@ua.pt", password);

        mvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(loginRequest)))
                .andExpect(status().isOk());
    }
}
