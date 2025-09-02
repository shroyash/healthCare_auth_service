package com.example.auth_service.controller;

import com.example.auth_service.dto.UserRegistrationRequest;
import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.UserDetails;
import com.example.auth_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRegistrationRequest request) {
        UserDetails savedUser = userService.registerUser(request);

        UserResponseDto response = new UserResponseDto(
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return ResponseEntity.ok(response);
    }
}
