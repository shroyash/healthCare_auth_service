package com.example.auth_service.controller;


import com.example.auth_service.config.JwtTokenProvider;
import com.example.auth_service.dto.ApiResponse;
import com.example.auth_service.dto.UpdateUsernameRequest;
import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @CookieValue(name = "jwt", required = true) String token) {

        String userName = jwtTokenProvider.getUsernameFromToken(token);
        Set<String> roles = jwtTokenProvider.getRolesFromToken(token);


        UserResponseDto userResponseDto = UserResponseDto.builder()
                .username(userName)
                .roles(roles)
                .build();


        ApiResponse<UserResponseDto> data = new ApiResponse<>(
                true,
                "Current user fetched successfully",
                userResponseDto
        );

        return ResponseEntity.ok(data);
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
