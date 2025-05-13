package com.getir.librarymanagementsystem.controller;

import com.getir.librarymanagementsystem.model.dto.response.UserResponse;
import com.getir.librarymanagementsystem.model.entity.User;
import com.getir.librarymanagementsystem.model.mapper.UserMapper;
import com.getir.librarymanagementsystem.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LIBRARIAN')")
@Tag(name = "User Management", description = "Endpoints for managing library users (LIBRARIAN access only)")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all users", description = "Retrieve a list of all registered users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Fetching all users");
        List<UserResponse> responses = userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
        log.debug("Retrieved {} users", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Fetch a user by their unique ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.debug("User found: {}", user.getUsername());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("User with ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user's details")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        log.info("Updating user with ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.debug("User found, updating fields...");
                    user.setUsername(updatedUser.getUsername());
                    user.setName(updatedUser.getName());
                    user.setContactInfo(updatedUser.getContactInfo());
                    user.setRole(updatedUser.getRole());
                    User saved = userRepository.save(user);
                    log.info("User with ID {} updated successfully", id);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> {
                    log.warn("User with ID {} not found for update", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by their unique ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User with ID {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        log.info("User with ID {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
