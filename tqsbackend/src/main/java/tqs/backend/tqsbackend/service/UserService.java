package tqs.backend.tqsbackend.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;

@Service

@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private static final EnumSet<UserRoles> SELF_REGISTER_ROLES = EnumSet.of(UserRoles.RENTER, UserRoles.OWNER);

    public User registerUser(String name, String email, String password, UserRoles role) {
        if (name.isBlank()) {
            logger.warn("Failed to register user: Invalid Name.");
            throw new IllegalArgumentException("Failed to register user: Invalid Name.");
        }
        if (email.isBlank() || !email.contains("@")) {
            String safeEmail = email.replaceAll("[\\n\\r]", "_");
            logger.warn("Failed to register user: Invalid Email {}.", safeEmail);
            throw new IllegalArgumentException("Failed to register user: Invalid Email.");
        }
        if (password.length() < 8) {
            logger.warn("Failed to register user: Password too short.");
            throw new IllegalArgumentException("Failed to register user: Password too short.");
        }

        // Check if email already exists
        String safeEmail = email.replaceAll("[\\n\\r]", "_");
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Failed to register user: Email {} already exists.", safeEmail);
            throw new IllegalArgumentException("Failed to register user: Email already exists.");
        }

        UserRoles sanitizedRole = resolveSelfServiceRole(role);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(name, email, hashedPassword, sanitizedRole);
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with ID {}", savedUser.getId());
        return savedUser;
    }

    private UserRoles resolveSelfServiceRole(UserRoles requestedRole) {
        if (requestedRole == null) {
            return UserRoles.RENTER;
        }
        if (!SELF_REGISTER_ROLES.contains(requestedRole)) {
            logger.warn("Failed to register user: Role {} is not self-assignable.", requestedRole);
            throw new IllegalArgumentException("Failed to register user: Role is not permitted.");
        }
        return requestedRole;
    }

    public boolean authenticate(String email, String password) {
        String safeEmail = email.replaceAll("[\\n\\r]", "_");
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User with email {} not found.", safeEmail);
            return false;
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Authentication failed: User with email {} is deactivated.", safeEmail);
            return false;
        }
        if (BCrypt.checkpw(password, user.getPassword())) {
            logger.info("User with email {} authenticated successfully.", safeEmail);
            return true;
        } else {
            logger.warn("Authentication failed: Incorrect password for email {}.", safeEmail);
            return false;
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(UserRoles role) {
        return userRepository.findByRole(role);
    }

    public List<User> getUsersByStatus(boolean isActive) {
        return userRepository.findByIsActive(isActive);
    }

    public List<User> getUsersByNameAndStatus(String name, boolean isActive) {
        return userRepository.findByNameContainingIgnoreCaseAndIsActive(name, isActive);
    }

    public List<User> getUsersByNameAndRole(String name, UserRoles role) {
        return userRepository.findByNameContainingIgnoreCaseAndRole(name, role);
    }

    public List<User> getUsersByRoleAndStatus(UserRoles role, boolean isActive) {
        return userRepository.findByRoleAndIsActive(role, isActive);
    }

    public List<User> getUsersByNameAndRoleAndStatus(String name, UserRoles role, boolean isActive) {
        return userRepository.findByNameContainingIgnoreCaseAndRoleAndIsActive(name, role, isActive);
    }

    public boolean deactivateUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            logger.warn("Deactivation failed: User with ID {} not found.", id);
            return false;
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Deactivation failed: User with ID {} is already inactive.", id);
            return false;
        }
        user.setActive(false);
        userRepository.save(user);
        logger.info("User with ID {} deactivated successfully.", id);
        return true;
    }

    public boolean activateUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            logger.warn("Activation failed: User with ID {} not found.", id);
            return false;
        }
        User user = userOpt.get();
        if (user.isActive()) {
            logger.warn("Activation failed: User with ID {} is already active.", id);
            return false;
        }
        user.setActive(true);
        userRepository.save(user);
        logger.info("User with ID {} activated successfully.", id);
        return true;
    }

    public List<User> searchUsers(String keyword, UserRoles role, java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate) {
        return userRepository.searchUsers(keyword, role, startDate, endDate);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

}
