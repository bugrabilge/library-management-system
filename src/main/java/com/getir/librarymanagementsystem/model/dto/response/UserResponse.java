package com.getir.librarymanagementsystem.model.dto.response;

import com.getir.librarymanagementsystem.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String contactInfo;
    private Role role;
}
