package com.example.auth_service.controller;


import com.example.auth_service.dto.UpdateUsernameRequest;
import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    // Get logged-in user profile
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getProfile(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(userService.getProfile(currentUser));
    }

    @PutMapping("/me/username")
    public ResponseEntity<UserResponseDto> changeUsername(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestBody UpdateUsernameRequest request
    ) {
        UserResponseDto updatedUser = userService.changeUsername(currentUser, request.getNewUsername());
        return ResponseEntity.ok(updatedUser);
    }

}
