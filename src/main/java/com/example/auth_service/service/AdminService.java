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
    
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    UserResponseDto getUserById(UUID id);

    void deleteUser(UUID id);

    UserResponseDto changeUserRole(UUID userId, RoleName newRole);

    List<DoctorRequestDto> getPendingDoctorRequests();

    List<DoctorRequestDto> getAllDoctorRequests();

    DoctorRequestResponse setRejectOrAccept( long doctorReqId, boolean approve);
}
