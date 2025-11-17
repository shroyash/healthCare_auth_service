package com.example.auth_service.dto;

import com.example.auth_service.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;       // JWT token
    private String tokenType;   // Typically "Bearer"
    private String username;
    private String email;
    private Set<Role> role;
}
