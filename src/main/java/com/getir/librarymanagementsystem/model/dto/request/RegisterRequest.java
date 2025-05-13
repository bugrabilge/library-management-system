package com.getir.librarymanagementsystem.model.dto.request;

import com.getir.librarymanagementsystem.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String contactInfo;
    private Role role;
}
