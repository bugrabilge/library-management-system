package com.getir.librarymanagementsystem.model.mapper;

import com.getir.librarymanagementsystem.model.dto.request.RegisterRequest;
import com.getir.librarymanagementsystem.model.dto.response.UserResponse;
import com.getir.librarymanagementsystem.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .contactInfo(request.getContactInfo())
                .role(request.getRole())
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .contactInfo(user.getContactInfo())
                .role(user.getRole())
                .build();
    }

}
