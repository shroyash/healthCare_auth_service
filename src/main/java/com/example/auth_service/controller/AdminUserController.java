package com.example.auth_service.controller;

import com.example.auth_service.dto.response.ApiResponse;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.enums.RoleName;
import com.example.auth_service.service.AdminUserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(new ApiResponse<>(true, "All users fetched successfully",
                adminUserService.getAllUsers(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully",
                adminUserService.getUserById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponseDto>> changeUserRole(
            @PathVariable UUID id,
            @RequestParam RoleName newRole) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User role updated successfully",
                adminUserService.changeUserRole(id, newRole)));
    }
}