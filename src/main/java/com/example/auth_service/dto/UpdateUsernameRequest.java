package com.example.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUsernameRequest {
    @NotBlank(message = "Username cannot be empty")
    private String newUsername;
}
