package com.getir.librarymanagementsystem.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private UserDetails userDetails;
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        validToken = jwtUtil.generateToken(userDetails);

        Map<String, Object> claims = new HashMap<>();
        claims.put("iat", new Date(System.currentTimeMillis() - 10000000L)); // issued at long ago
        claims.put("exp", new Date(System.currentTimeMillis() - 5000L)); // expired 5 seconds ago
        expiredToken = jwtUtil.generateToken(claims, userDetails);
    }

    @Test
    void extractUsername_ShouldReturnUsernameFromValidToken() {
        String username = jwtUtil.extractUsername(validToken);
        assertEquals(userDetails.getUsername(), username);
    }

    @Test
    void generateToken_ShouldCreateValidJwtStructure() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generateTokenWithClaims_ShouldIncludeAllClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("email", "test@example.com");

        String token = jwtUtil.generateToken(claims, userDetails);
        String username = jwtUtil.extractUsername(token);

        assertEquals(userDetails.getUsername(), username);
    }

    @Test
    void isTokenValid_ShouldReturnTrueForValidToken() {
        assertTrue(jwtUtil.isTokenValid(validToken, userDetails));
    }

    @Test
    void isTokenValid_ShouldReturnFalseForWrongUser() {
        UserDetails otherUser = User.builder()
                .username("otheruser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        assertFalse(jwtUtil.isTokenValid(validToken, otherUser));
    }

    @Test
    void isTokenValid_ShouldReturnFalseForMalformedToken() {
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY1MTIzNDU2NywiZXhwIjoxNjUxMjM0NTY4fQ." +
                "invalid-signature";

        try {
            assertFalse(jwtUtil.isTokenValid(malformedToken, userDetails));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("signature"));
        }
    }

    @Test
    void isTokenValid_ShouldHandleEmptyToken() {
        try {
            assertFalse(jwtUtil.isTokenValid("", userDetails));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("JWT String argument cannot be null or empty"));
        }
    }

    @Test
    void isTokenValid_ShouldHandleNullToken() {
        try {
            assertFalse(jwtUtil.isTokenValid(null, userDetails));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("JWT String argument cannot be null or empty"));
        }
    }
}