package tqs.backend.tqsbackend.controller.pict;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PICT-based Parameterized Tests for User Search (Admin Feature)
 * 
 * This test class uses PICT to generate test cases that cover all pairwise
 * combinations of user search parameters.
 * 
 * PICT Model: tqsbackend/pict-models/user_search.pict
 * Generated Cases: 18 test combinations
 * 
 * Parameters tested:
 * - name: NULL, "TestUser", "Partial", "NonExistent"
 * - role: NULL, "RENTER", "OWNER", "ADMIN"
 * - active: NULL, "true", "false"
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UserSearchPICTTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void resetDb() {
        userRepository.deleteAll();
    }

    /**
     * PICT Test: Tests all pairwise combinations of user search parameters
     * 
     * CSV Format: name,role,active
     * Test count: 18 combinations
     */
    @ParameterizedTest(name = "#{index}: name={0}, role={1}, active={2}")
    @CsvFileSource(resources = "/pict/user_search_cases.csv", numLinesToSkip = 1)
    public void testUserSearchPICT(String name, String role, String active) throws Exception {
        // Create test users
        createUser("TestUser", "testuser@test.com", UserRoles.RENTER, true);
        createUser("PartialMatch User", "partial@test.com", UserRoles.OWNER, true);
        createUser("Another Partial", "another@ test.com", UserRoles.ADMIN, false);

        // Build request
        var request = get("/api/users/search")
                .contentType(MediaType.APPLICATION_JSON);

        // Add parameters if not NULL
        if (!"NULL".equals(name) && name != null) {
            String cleanName = name.replace("\"", "");
            request.param("name", cleanName);
        }
        if (!"NULL".equals(role) && role != null) {
            String cleanRole = role.replace("\"", "");
            request.param("role", cleanRole);
        }
        if (!"NULL".equals(active) && active != null) {
            String cleanActive = active.replace("\"", "");
            request.param("active", cleanActive);
        }

        // Execute request and verify response
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Note: We don't check exact results as PICT is testing parameter combinations,
        // not business logic. The goal is to ensure no errors with various parameter
        // combos.
    }

    private void createUser(String name, String email, UserRoles role, boolean active) {
        User user = new User(name, email, "password123", role);
        user.setActive(active);
        userRepository.save(user);
    }
}
