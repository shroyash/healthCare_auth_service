package com.example.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private UUID userId;
    private String username;
    private String email;
    private Set<String> roles; // multiple roles as Strings
}
