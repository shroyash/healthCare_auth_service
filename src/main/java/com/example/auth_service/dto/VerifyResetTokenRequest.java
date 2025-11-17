package com.example.auth_service.dto;

import lombok.Data;

@Data
public class VerifyResetTokenRequest {
    private String email;
    private String token;
}
