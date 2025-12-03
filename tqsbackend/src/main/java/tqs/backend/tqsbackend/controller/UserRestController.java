package tqs.backend.tqsbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.backend.tqsbackend.dto.UserDto;
import tqs.backend.tqsbackend.dto.UserLoginRequest;
import tqs.backend.tqsbackend.dto.UserRegistrationDto;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto userDto) {
        try {
            User created = userService.registerUser(userDto.getName(), userDto.getEmail(), userDto.getPassword(),
                    userDto.getRole());
            return new ResponseEntity<>(convertToDto(created), HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest credentials) {
        boolean success = userService.authenticate(credentials.getEmail(), credentials.getPassword());
        if (success) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<UserDto> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UserRoles role,
            @RequestParam(required = false) Boolean active) {
        List<User> users;
        if (name != null && role != null && active != null) {
            users = userService.getUsersByNameAndRoleAndStatus(name, role, active);
        } else if (name != null && active != null) {
            users = userService.getUsersByNameAndStatus(name, active);
        } else if (name != null && role != null) {
            users = userService.getUsersByNameAndRole(name, role);
        } else if (role != null && active != null) {
            users = userService.getUsersByRoleAndStatus(role, active);
        } else if (name != null) {
            users = userService.getUsersByName(name);
        } else if (role != null) {
            users = userService.getUsersByRole(role);
        } else if (active != null) {
            users = userService.getUsersByStatus(active);
        } else {
            users = userService.getAllUsers();
        }
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private UserDto convertToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }
}
