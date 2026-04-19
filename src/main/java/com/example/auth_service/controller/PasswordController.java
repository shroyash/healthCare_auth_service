package com.example.auth_service.controller;

import com.example.auth_service.dto.request.ChangePasswordRequest;
import com.example.auth_service.dto.request.ForgotPasswordRequest;
import com.example.auth_service.dto.request.ResetPasswordRequest;
import com.example.auth_service.dto.request.VerifyResetTokenRequest;
import com.example.auth_service.dto.response.ApiResponse;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.service.PasswordService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse<String>> forgetPassword(
            @RequestBody ForgotPasswordRequest request) {
        passwordService.forgetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "OTP sent to your email", null));
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<String>> verifyResetToken(
            @RequestBody VerifyResetTokenRequest request) {
        boolean isValid = passwordService.verifyResetToken(request);
        if (isValid) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Token is valid", null));
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid or expired token", null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", null));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AppUser currentUser) {
        passwordService.changePassword(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
    }
}