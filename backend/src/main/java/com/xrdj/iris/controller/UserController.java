package com.xrdj.iris.controller;

import com.xrdj.iris.dto.CreateUserRequest;
import com.xrdj.iris.dto.UpdateUserRequest;
import com.xrdj.iris.dto.UserDto;
import com.xrdj.iris.model.Role;
import com.xrdj.iris.model.User;
import com.xrdj.iris.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users =
                userRepository.findAll().stream()
                        .map(
                                user ->
                                        new UserDto(
                                                user.getId(),
                                                user.getUsername(),
                                                user.getRole().name()))
                        .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Attempted to create user with existing username: {}", request.getUsername());
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        Role role = Role.ROLE_USER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
            role = Role.ROLE_ADMIN;
        }

        User user =
                User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(role)
                        .build();

        userRepository.save(user);
        log.info("User created successfully: {}", user.getUsername());
        return ResponseEntity.ok("User registered successfully!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        user.setUsername(request.getUsername());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
            user.setRole(Role.ROLE_ADMIN);
        } else {
            user.setRole(Role.ROLE_USER);
        }

        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
        return ResponseEntity.ok("User deleted successfully!");
    }
}
