package com.example.auth_service.controller;

import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequestDto;
import com.example.auth_service.dto.UserRegistrationRequest;
import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ---------------- Register ----------------
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRegistrationRequest request) {
        AppUser savedUser = authService.registerUser(request);

        UserResponseDto response = new UserResponseDto(
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles().stream()
                        .map(role -> role.getName().name()) // convert enum to String
                        .collect(Collectors.toSet())

        );

        return ResponseEntity.ok(response);
    }

    // ---------------- Login ----------------
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginUser(@RequestBody LoginRequestDto request) {
        JwtResponse jwtResponse = authService.loginUser(request);
        return ResponseEntity.ok(jwtResponse);
    }
}
