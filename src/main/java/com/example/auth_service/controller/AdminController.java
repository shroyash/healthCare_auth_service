package com.example.auth_service.controller;

import com.example.auth_service.dto.DoctorRequestDto;
import com.example.auth_service.dto.DoctorRequestResponse;
import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.DoctorRequest;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // List all users (with pagination)
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    // Get user by ID
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    // Delete a user
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Change user role
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> changeUserRole(
            @PathVariable Long id,
            @RequestParam RoleName newRole
    ) {
        return ResponseEntity.ok(adminService.changeUserRole(id, newRole));
    }

    @GetMapping("/doctor-req/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DoctorRequestDto>> getAll() {
        return ResponseEntity.ok(adminService.getAllDoctorRequests());
    }

    @GetMapping("/doctor-req/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DoctorRequestDto>> getPending() {
        return ResponseEntity.ok(adminService.getPendingDoctorRequests());
    }


    @PostMapping("/doctor-req/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorRequestResponse> approveOrRejectDoctor(
            @RequestParam Long doctorReqId,
            @RequestParam boolean approve
    ) {
        DoctorRequestResponse response = adminService.setRejectOrAccept(doctorReqId, approve);
        return ResponseEntity.ok(response);
    }




}
