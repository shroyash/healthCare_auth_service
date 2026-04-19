package com.example.auth_service.controller;

import com.example.auth_service.dto.request.DoctorRequestDto;
import com.example.auth_service.dto.response.ApiResponse;
import com.example.auth_service.dto.response.DoctorRequestResponse;
import com.example.auth_service.service.DoctorRequestService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctor-requests")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class DoctorRequestController {

    private final DoctorRequestService doctorRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorRequestDto>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "All doctor requests fetched",
                doctorRequestService.getAllDoctorRequests()));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<DoctorRequestDto>>> getPending() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending doctor requests fetched",
                doctorRequestService.getPendingDoctorRequests()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<DoctorRequestResponse>> approve(@PathVariable long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Doctor request approved",
                doctorRequestService.approveDoctor(id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<DoctorRequestResponse>> reject(@PathVariable long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Doctor request rejected",
                doctorRequestService.rejectDoctor(id)));
    }

    @GetMapping("/pending-count")
    public ResponseEntity<ApiResponse<Integer>> getPendingCount() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending doctor requests count",
                doctorRequestService.getPendingDoctorCount()));
    }
}