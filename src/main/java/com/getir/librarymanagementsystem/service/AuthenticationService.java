package com.getir.librarymanagementsystem.service;

import com.getir.librarymanagementsystem.model.dto.request.AuthenticationRequest;
import com.getir.librarymanagementsystem.model.dto.request.RegisterRequest;
import com.getir.librarymanagementsystem.model.dto.response.AuthenticationResponse;
import com.getir.librarymanagementsystem.model.entity.User;
import com.getir.librarymanagementsystem.model.mapper.UserMapper;
import com.getir.librarymanagementsystem.repository.UserRepository;
import com.getir.librarymanagementsystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: username '{}' is already taken.", request.getUsername());
            throw new IllegalStateException("Username is already taken.");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        log.info("User registered successfully: {}", request.getUsername());

        String jwt = jwtUtil.generateToken(user);
        return new AuthenticationResponse(jwt);
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("Authentication failed: user '{}' not found", request.getUsername());
                    return new IllegalArgumentException("User not found");
                });

        log.info("Authentication successful for user: {}", request.getUsername());

        String jwt = jwtUtil.generateToken(user);
        return new AuthenticationResponse(jwt);
    }

}
