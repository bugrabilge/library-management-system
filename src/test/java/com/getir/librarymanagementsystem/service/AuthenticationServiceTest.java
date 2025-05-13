package com.getir.librarymanagementsystem.service;

import com.getir.librarymanagementsystem.model.dto.request.AuthenticationRequest;
import com.getir.librarymanagementsystem.model.dto.request.RegisterRequest;
import com.getir.librarymanagementsystem.model.dto.response.AuthenticationResponse;
import com.getir.librarymanagementsystem.model.entity.Role;
import com.getir.librarymanagementsystem.model.entity.User;
import com.getir.librarymanagementsystem.model.mapper.UserMapper;
import com.getir.librarymanagementsystem.repository.UserRepository;
import com.getir.librarymanagementsystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldSaveUserSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("testpass")
                .name("Test User")
                .contactInfo("test@example.com")
                .role(Role.PATRON)
                .build();

        User mappedUser = User.builder()
                .username("testuser")
                .password("hashedpass")
                .name("Test User")
                .contactInfo("test@example.com")
                .role(Role.PATRON)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("testpass")).thenReturn("hashedpass");

        assertDoesNotThrow(() -> authenticationService.register(request));
        verify(userRepository).save(mappedUser);
    }

    @Test
    void register_shouldFailIfUsernameAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existingUser")
                .password("password")
                .name("Existing User")
                .contactInfo("email@example.com")
                .role(Role.PATRON)
                .build();

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalStateException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailIfMapperReturnsNull() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("testpass")
                .name("Test User")
                .contactInfo("test@example.com")
                .role(Role.PATRON)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> authenticationService.register(request));
    }

    @Test
    void authenticate_shouldReturnToken() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("hashedpass")
                .role(Role.PATRON)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("mockedToken");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("mockedToken", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_shouldFailIfUserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("nonexistent");
        request.setPassword("pass");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(request));
    }

}
