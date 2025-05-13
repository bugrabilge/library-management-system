package com.getir.librarymanagementsystem.controller;

import com.getir.librarymanagementsystem.model.dto.request.AuthenticationRequest;
import com.getir.librarymanagementsystem.model.dto.request.RegisterRequest;
import com.getir.librarymanagementsystem.model.dto.response.AuthenticationResponse;
import com.getir.librarymanagementsystem.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate a user and return a JWT token")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);
        log.info("Login successful for username: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Register a new user into the system")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        log.info("Register request received for username: {}", request.getUsername());
        authenticationService.register(request);
        log.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity.ok("User registered successfully.");
    }
}
