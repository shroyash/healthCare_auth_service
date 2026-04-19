package com.example.auth_service.controller;

import com.example.auth_service.dto.request.LoginRequestDto;
import com.example.auth_service.dto.response.ApiResponse;
import com.example.auth_service.dto.response.LoginResponseDto;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.service.AuthsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthsController {

    private final AuthsService authService;

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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal AppUser currentUser) {
        UserResponseDto dto = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Current user fetched successfully", dto));
    }
}