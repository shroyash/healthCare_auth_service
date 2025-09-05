package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;       // JWT token
    private String tokenType;   // Typically "Bearer"
    private String username;
    private String email;
}
