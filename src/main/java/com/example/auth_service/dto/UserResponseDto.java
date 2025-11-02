package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String username;
    private String email;
    private Set<String> roles; // multiple roles as Strings
}
