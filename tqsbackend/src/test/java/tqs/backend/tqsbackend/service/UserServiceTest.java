package tqs.backend.tqsbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mindrot.jbcrypt.BCrypt;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User validUser;
    private User inactiveUser;

    private final String plainPassword = "password1";

    @BeforeEach
    void setUp() {
        String encodedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        validUser = new User("John Doe", "john@ua.pt", encodedPassword, UserRoles.RENTER);
        validUser.setId(1L);
        validUser.setActive(true);

        inactiveUser = new User("Inactive John", "inactive@ua.pt", encodedPassword, UserRoles.RENTER);
        inactiveUser.setId(2L);
        inactiveUser.setActive(false);
    }

    @Test
    void testRegisterUser() {
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        User result = userService.registerUser("John Doe", "john@ua.pt", plainPassword, UserRoles.RENTER);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@ua.pt");
        assertThat(result.getName()).isEqualTo("John Doe");

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("", "email@ua.pt", "pass1234", UserRoles.RENTER));
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("John", "invalid-email", "pass1234", UserRoles.RENTER));
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("John", "email@ua.pt", "short", UserRoles.RENTER));
    }

    @Test
    void testRegisterUserDefaultsToRenterWhenRoleMissing() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            return user;
        });

        User registered = userService.registerUser("Jane Doe", "jane@ua.pt", plainPassword, null);

        assertThat(registered.getRole()).isEqualTo(UserRoles.RENTER);
    }

    @Test
    void testRegisterUserRejectsAdminRole() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("Admin", "admin@ua.pt", plainPassword, UserRoles.ADMIN));
    }

    @Test
    void testAuthenticate() {
        when(userRepository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));
        when(userRepository.findByEmail("sample@ua.pt")).thenReturn(Optional.empty());

        boolean loginSuccess = userService.authenticate("john@ua.pt", plainPassword);
        assertThat(loginSuccess).isTrue();

        boolean loginWrongPass = userService.authenticate("john@ua.pt", "wrongPass");
        assertThat(loginWrongPass).isFalse();

        boolean loginGhost = userService.authenticate("sample@ua.pt", plainPassword);
        assertThat(loginGhost).isFalse();
    }

    @Test
    void testSingleUserGetters() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.findByEmail("john@ua.pt")).thenReturn(Optional.of(validUser));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> foundById = userService.getUserById(1L);
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getName()).isEqualTo("John Doe");

        Optional<User> foundByEmail = userService.getUserByEmail("john@ua.pt");
        assertThat(foundByEmail).isPresent();

        Optional<User> notFound = userService.getUserById(99L);
        assertThat(notFound).isEmpty();
    }

    @Test
    void testListGetters() {
        List<User> userList = List.of(validUser);

        when(userRepository.findAll()).thenReturn(userList);
        when(userRepository.findByNameContainingIgnoreCase("John")).thenReturn(userList);
        when(userRepository.findByRole(UserRoles.RENTER)).thenReturn(userList);
        when(userRepository.findByIsActive(true)).thenReturn(userList);

        when(userRepository.findByNameContainingIgnoreCaseAndIsActive("John", true)).thenReturn(userList);
        when(userRepository.findByNameContainingIgnoreCaseAndRole("John", UserRoles.RENTER)).thenReturn(userList);
        when(userRepository.findByRoleAndIsActive(UserRoles.RENTER, true)).thenReturn(userList);
        when(userRepository.findByNameContainingIgnoreCaseAndRoleAndIsActive("John", UserRoles.RENTER, true))
                .thenReturn(userList);

        assertThat(userService.getAllUsers()).hasSize(1);
        assertThat(userService.getUsersByName("John")).hasSize(1);
        assertThat(userService.getUsersByRole(UserRoles.RENTER)).hasSize(1);
        assertThat(userService.getUsersByStatus(true)).hasSize(1);

        assertThat(userService.getUsersByNameAndStatus("John", true)).isNotEmpty();
        assertThat(userService.getUsersByNameAndRole("John", UserRoles.RENTER)).isNotEmpty();
        assertThat(userService.getUsersByRoleAndStatus(UserRoles.RENTER, true)).isNotEmpty();
        assertThat(userService.getUsersByNameAndRoleAndStatus("John", UserRoles.RENTER, true)).isNotEmpty();
    }

    @Test
    void testStateManagement() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        boolean deactSuccess = userService.deactivateUser(1L);
        assertThat(deactSuccess).isTrue();
        assertThat(validUser.isActive()).isFalse();

        boolean deactFail = userService.deactivateUser(2L);
        assertThat(deactFail).isFalse();

        boolean deactNotFound = userService.deactivateUser(99L);
        assertThat(deactNotFound).isFalse();

        inactiveUser.setActive(false);

        boolean actSuccess = userService.activateUser(2L);
        assertThat(actSuccess).isTrue();
        assertThat(inactiveUser.isActive()).isTrue();

        boolean actNotFound = userService.activateUser(99L);
        assertThat(actNotFound).isFalse();
    }
}