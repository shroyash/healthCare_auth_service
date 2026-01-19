package com.example.auth_service.service;

import com.example.auth_service.dto.request.DoctorRequestDto;
import com.example.auth_service.dto.response.DoctorRequestResponse;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    // List all users with pagination and optional filters
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    // Get a specific user by ID
    UserResponseDto getUserById(UUID id);

    // Delete or deactivate a user
    void deleteUser(UUID id);

    // Change user role
    UserResponseDto changeUserRole(UUID userId, RoleName newRole);

    List<DoctorRequestDto> getPendingDoctorRequests();

    List<DoctorRequestDto> getAllDoctorRequests();

    DoctorRequestResponse setRejectOrAccept( long doctorReqId, boolean approve);

}
