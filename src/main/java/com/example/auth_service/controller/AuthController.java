package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/patient")
    public ResponseEntity<UserResponseDto> registerPatient(@RequestBody UserRegistrationRequest request) {
        AppUser savedUser = authService.registerUser(request);

        UserResponseDto response = new UserResponseDto(
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<String> registerDoctor(@RequestBody DoctorRegistrationRequest request) {
        authService.registerDoctor(request);
        return ResponseEntity.ok("Your doctor registration request has been sent to admin. Please wait for approval in email.");
    }

    // Original token-based login (keep for API clients)
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginUser(@RequestBody LoginRequestDto request) {
        JwtResponse jwtResponse = authService.loginUser(request);
        return ResponseEntity.ok(jwtResponse);
    }

    // New cookie-based login for web clients
    @PostMapping("/login-web")
    public ResponseEntity<LoginResponseDto> loginUserWeb(
            @RequestBody LoginRequestDto request,
            HttpServletResponse response) {

        LoginResponseDto loginResponse = authService.loginUserWithCookie(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    // Logout endpoint for web clients
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgotPasswordRequest request){
        authService.forgetPassword(request);
        return ResponseEntity.ok("OTP has been sent to your email successfully");
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<?> verifyResetToken(@RequestBody VerifyResetTokenRequest request) {
        boolean isValid = authService.verifyResetToken(request);

        if (isValid) {
            return ResponseEntity.ok("Token is valid");
        } else {
            return ResponseEntity.status(400).body("Invalid or expired token");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

    @PutMapping("/users/me/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        // currentUser is automatically the logged-in user
        authService.changePassword(currentUser, request);
        return ResponseEntity.ok("Password changed successfully");
    }
}