package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.model.DoctorRequestStatus;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.repository.DoctorReqRepository;
import com.example.auth_service.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DoctorReqRepository doctorReqRepository;

    // List all users (with pagination)
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(Pageable pageable) {
        Page<UserResponseDto> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "All users fetched successfully", users));
    }

    // Get user by ID
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable UUID id) {
        UserResponseDto user = adminService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", user));
    }


    // Delete a user
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
    }

    // Change user role
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> changeUserRole(
            @PathVariable UUID id,
            @RequestParam RoleName newRole
    ) {
        UserResponseDto updatedUser = adminService.changeUserRole(id, newRole);
        return ResponseEntity.ok(new ApiResponse<>(true, "User role updated successfully", updatedUser));
    }

    // Get all doctor requests
    @GetMapping("/doctor-req/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DoctorRequestDto>>> getAll() {
        List<DoctorRequestDto> requests = adminService.getAllDoctorRequests();
        return ResponseEntity.ok(new ApiResponse<>(true, "All doctor requests fetched", requests));
    }

    // Get only pending doctor requests
    @GetMapping("/doctor-req/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DoctorRequestDto>>> getPending() {
        List<DoctorRequestDto> pending = adminService.getPendingDoctorRequests();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending doctor requests fetched", pending));
    }

    // Approve or reject doctor
    @PostMapping("/doctor-req/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorRequestResponse>> approveOrRejectDoctor(
            @CookieValue("jwt") String token,
            @RequestParam Long doctorReqId,
            @RequestParam boolean approve
    ) {
        DoctorRequestResponse response = adminService.setRejectOrAccept(token,doctorReqId, approve);
        String message = approve ? "Doctor request approved" : "Doctor request rejected";
        return ResponseEntity.ok(new ApiResponse<>(true, message, response));
    }

    @GetMapping("/pending-doctors-count")
    public ResponseEntity<ApiResponse<Integer>> getTotalDoctorRequest() {
        long count = doctorReqRepository.countByStatus(DoctorRequestStatus.PENDING);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending doctor requests count", (int) count));
    }

}

