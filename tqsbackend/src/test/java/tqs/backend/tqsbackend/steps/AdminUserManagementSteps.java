package tqs.backend.tqsbackend.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;
import tqs.backend.tqsbackend.service.UserService;
import tqs.backend.tqsbackend.pages.AdminDashboardPage;
import tqs.backend.tqsbackend.pages.LoginPage;
import tqs.backend.tqsbackend.pages.UserManagementPage;
import tqs.backend.tqsbackend.pages.UserProfilePage;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminUserManagementSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private Page page;
    private LoginPage loginPage;
    private AdminDashboardPage adminDashboardPage;
    private UserManagementPage userManagementPage;
    private UserProfilePage userProfilePage;
    private final PlaywrightSteps playwrightSteps;

    public AdminUserManagementSteps(PlaywrightSteps playwrightSteps) {
        this.playwrightSteps = playwrightSteps;
    }

    @Before(value = "@admin_user_management", order = 2)
    public void setup() {
        userRepository.deleteAll();
        this.page = playwrightSteps.getPage();
        this.loginPage = new LoginPage(page);
        this.adminDashboardPage = new AdminDashboardPage(page);
        this.userManagementPage = new UserManagementPage(page);
        this.userProfilePage = new UserProfilePage(page);
    }

    @Given("I am logged in as an admin")
    public void i_am_logged_in_as_an_admin() {
        String adminEmail = "admin@partyshare.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt());
            User admin = new User("Admin User", adminEmail, hashedPassword, UserRoles.ADMIN);
            userRepository.save(admin);
        }

        loginPage.navigate("http://localhost:" + port + "/users/login");
        loginPage.login(adminEmail, "admin123");
        // Wait for dashboard
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Admin Dashboard")).waitFor();
    }

    @Given("a user exists with name {string}, email {string}, and role {string}")
    public void a_user_exists_with_details(String name, String email, String roleStr) {
        if (userRepository.findByEmail(email).isEmpty()) {
            UserRoles role = UserRoles.valueOf(roleStr);
            userService.registerUser(name, email, "password", role);
        }
    }

    @Given("I am on the admin dashboard page")
    public void i_am_on_the_admin_dashboard_page() {
        if (!page.url().contains("/admin/dashboard")) {
            page.navigate("http://localhost:" + port + "/admin/dashboard");
        }
    }

    @When("I navigate to User Management")
    public void i_navigate_to_user_management() {
        adminDashboardPage.navigateToUserManagement();
    }

    @When("I search for {string}")
    public void i_search_for(String keyword) {
        userManagementPage.searchForUser(keyword);
    }

    @When("I view the profile of {string}")
    public void i_view_the_profile_of(String userName) {
        userManagementPage.viewProfile(userName);
    }

    @When("I click \"Deactivate User\"")
    public void i_click_deactivate_user() {
        userProfilePage.deactivateUser();
    }

    @Then("the user {string} should be \"Inactive\"")
    public void the_user_should_be_inactive(String userName) {
        assertTrue(userProfilePage.isUserInactive());
    }
}
