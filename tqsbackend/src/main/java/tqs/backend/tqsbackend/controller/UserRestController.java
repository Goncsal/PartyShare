package tqs.backend.tqsbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User created = userService.registerUser(user.getName(), user.getEmail(), user.getPassword(), user.getRole());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        boolean success = userService.authenticate(credentials.get("email"), credentials.get("password"));
        if (success) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UserRoles role,
            @RequestParam(required = false) Boolean active) {
        if (name != null && role != null && active != null) {
            return userService.getUsersByNameAndRoleAndStatus(name, role, active);
        } else if (name != null && active != null) {
            return userService.getUsersByNameAndStatus(name, active);
        } else if (name != null && role != null) {
            return userService.getUsersByNameAndRole(name, role);
        } else if (role != null && active != null) {
            return userService.getUsersByRoleAndStatus(role, active);
        } else if (name != null) {
            return userService.getUsersByName(name);
        } else if (role != null) {
            return userService.getUsersByRole(role);
        } else if (active != null) {
            return userService.getUsersByStatus(active);
        }
        return userService.getAllUsers();
    }
}
