package com.example.auth_service.dto.request;

import lombok.Data;

@Data
public class VerifyResetTokenRequest {
    private String email;
    private String token;
}
