package com.example.auth_service.controller;

import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // List all users (with pagination)
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    // Get user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    // Delete a user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Change user role
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponseDto> changeUserRole(
            @PathVariable Long id,
            @RequestParam RoleName newRole
    ) {
        return ResponseEntity.ok(adminService.changeUserRole(id, newRole));
    }
}
