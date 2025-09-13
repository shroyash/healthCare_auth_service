package com.example.auth_service.service;

import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    // List all users with pagination and optional filters
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    // Get a specific user by ID
    UserResponseDto getUserById(Long id);

    // Delete or deactivate a user
    void deleteUser(Long id);

    // Change user role
    UserResponseDto changeUserRole(Long userId, RoleName newRole);
}
