package com.example.auth_service.controller;

import com.example.auth_service.dto.request.*;
import com.example.auth_service.dto.response.ApiResponse;
import com.example.auth_service.dto.response.JwtResponse;
import com.example.auth_service.dto.response.LoginResponseDto;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/patient")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerPatient(@RequestBody UserRegistrationRequest request) {
        AppUser savedUser = authService.registerUser(request);

        UserResponseDto response = new UserResponseDto(
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Patient registered successfully", response));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<ApiResponse<String>> registerDoctor(
            @ModelAttribute DoctorRegistrationRequest request,
            BindingResult result
    ) {
        authService.registerDoctor(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Doctor registration request sent", null));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> loginUser(@RequestBody LoginRequestDto request) {
        JwtResponse jwtResponse = authService.loginUser(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", jwtResponse));
    }

    @PostMapping("/login-web")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginUserWeb(
            @RequestBody LoginRequestDto request,
            HttpServletResponse response) {

        LoginResponseDto loginResponse = authService.loginUserWithCookie(request, response);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful (web)", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully", null));
    }

    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse<String>> forgetPassword(@RequestBody ForgotPasswordRequest request){
        authService.forgetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "OTP sent to your email", null));
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<String>> verifyResetToken(@RequestBody VerifyResetTokenRequest request) {
        boolean isValid = authService.verifyResetToken(request);

        if (isValid) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Token is valid", null));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid or expired token", null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", null));
    }

    @PutMapping("/users/me/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        authService.changePassword(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
    }
}
