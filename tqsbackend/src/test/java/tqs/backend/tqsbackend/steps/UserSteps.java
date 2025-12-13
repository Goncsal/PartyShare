package tqs.backend.tqsbackend.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.service.UserService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest
public class UserSteps {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User registeredUser;
    private Exception registrationException;
    private String name;
    private String email;
    private String password;
    private UserRoles role;

    @Given("I have valid registration details")
    public void i_have_valid_registration_details(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> row = data.get(0);
        this.name = row.get("name");
        this.email = row.get("email");
        this.password = row.get("password");
        this.role = UserRoles.valueOf(row.get("role"));

        // Ensure user does not exist
        if (userRepository.findByEmail(email).isPresent()) {
            userRepository.delete(userRepository.findByEmail(email).get());
        }
    }

    @When("I register with these details")
    public void i_register_with_these_details() {
        try {
            registeredUser = userService.registerUser(name, email, password, role);
        } catch (Exception e) {
            registrationException = e;
        }
    }

    @Then("the registration should be successful")
    public void the_registration_should_be_successful() {
        assertNull(registrationException);
        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
    }

    @Then("the user should be in the system")
    public void the_user_should_be_in_the_system() {
        assertTrue(userRepository.findByEmail(email).isPresent());
    }

    @Given("a user exists with email {string}")
    public void a_user_exists_with_email(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User("Existing User", email, "password", UserRoles.RENTER);
            userRepository.save(user);
        }
    }

    @When("I register with name {string}, email {string}, password {string}, and role {string}")
    public void i_register_with_details(String name, String email, String password, String roleStr) {
        try {
            registeredUser = userService.registerUser(name, email, password, UserRoles.valueOf(roleStr));
        } catch (Exception e) {
            registrationException = e;
        }
    }

    @Then("the registration should fail with error {string}")
    public void the_registration_should_fail_with_error(String errorMessage) {
        assertNotNull(registrationException);
        assertEquals(errorMessage, registrationException.getMessage());
    }
}
