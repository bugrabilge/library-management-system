package com.getir.librarymanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getir.librarymanagementsystem.model.dto.response.UserResponse;
import com.getir.librarymanagementsystem.model.entity.Role;
import com.getir.librarymanagementsystem.model.entity.User;
import com.getir.librarymanagementsystem.model.mapper.UserMapper;
import com.getir.librarymanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserMapper userMapper; // UserMapper mock'u eklendi

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .name("Test User")
                .contactInfo("test@example.com")
                .role(Role.PATRON)
                .build();

        userResponse = UserResponse.builder()
                .username("testuser")
                .name("Test User")
                .contactInfo("test@example.com")
                .role(Role.PATRON)
                .build();
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getAllUsers_shouldReturnList() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(user));

        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[0].name", is("Test User")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getUserById_shouldReturnUser() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void updateUser_shouldUpdateUser() throws Exception {
        User updatedUser = User.builder()
                .id(1L)
                .username("updatedUser")
                .name("Updated Name")
                .contactInfo("updated@example.com")
                .role(Role.LIBRARIAN)
                .password("password") // Eğer User entity'de varsa
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse userResponse = UserResponse.builder()
                .username("updatedUser")
                .name("Updated Name")
                .contactInfo("updated@example.com")
                .role(Role.LIBRARIAN)
                .build();
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updatedUser")))
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void deleteUser_shouldDeleteSuccessfully() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
