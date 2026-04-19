package com.example.auth_service.controller;

import com.example.auth_service.dto.request.DoctorRegistrationRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.dto.response.ApiResponse;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/register")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/patient")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerPatient(
            @RequestBody UserRegistrationRequest request) {
        AppUser savedUser = registrationService.registerUser(request);
        UserResponseDto response = new UserResponseDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Patient registered successfully", response));
    }

    @PostMapping("/doctor")
    public ResponseEntity<ApiResponse<String>> registerDoctor(
            @ModelAttribute DoctorRegistrationRequest request) {
        registrationService.registerDoctor(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Doctor registration request sent", null));
    }
}