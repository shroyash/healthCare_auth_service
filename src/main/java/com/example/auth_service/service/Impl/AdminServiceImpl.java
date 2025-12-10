package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.DoctorRequestDto;
import com.example.auth_service.dto.DoctorRequestResponse;
import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.feign.HealthcareServiceClient;
import com.example.auth_service.globalExpection.RoleNotFoundExpection;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.*;
import com.example.auth_service.repository.DoctorReqRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AdminService;
import com.example.auth_service.service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorReqRepository doctorReqRepository;
    private final EmailService emailService;
    private final HealthcareServiceClient healthcareServiceClient;

    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserResponseDto(
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet())
                ));
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return new UserResponseDto(
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public void deleteUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public UserResponseDto changeUserRole(Long userId, RoleName newRole) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Role role = roleRepository.findByName(newRole)
                .orElseThrow(() -> new RoleNotFoundExpection("Role not found"));

        user.getRoles().clear();
        user.getRoles().add(role);

        userRepository.save(user);

        return new UserResponseDto(
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    // Get all doctor requests
    @Override
    public List<DoctorRequestDto> getAllDoctorRequests() {
        return doctorReqRepository.findAllWithUser()
                .stream()
                .map(dr -> DoctorRequestDto.builder()
                        .doctorReqId(dr.getDoctorReqId())
                        .userName(dr.getUser().getUsername())
                        .email(dr.getUser().getEmail())
                        .doctorLicence(dr.getDoctorLicence())
                        .status(dr.getStatus())
                        .build())
                .toList();
    }



    // get only pending requests
    @Override
    public List<DoctorRequestDto> getPendingDoctorRequests() {
        return doctorReqRepository.findByStatusWithUser(DoctorRequestStatus.PENDING)
                .stream()
                .map(dr -> DoctorRequestDto.builder()
                        .doctorReqId(dr.getDoctorReqId())
                        .userName(dr.getUser().getUsername())
                        .email(dr.getUser().getEmail())
                        .doctorLicence(dr.getDoctorLicence())
                        .status(dr.getStatus())
                        .build())
                .toList();
    }


    @Override
    public DoctorRequestResponse setRejectOrAccept(String token, Long doctorReqId, boolean approve) {
        DoctorRequest request = doctorReqRepository.findById(doctorReqId)
                .orElseThrow(() -> new RuntimeException("Doctor request not found"));

        AppUser user = request.getUser();

        if (approve) {
            request.setStatus(DoctorRequestStatus.APPROVED);

            Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR)
                    .orElseThrow(() -> new RoleNotFoundExpection("Role not found"));

            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            user.getRoles().add(doctorRole);

            doctorReqRepository.save(request);
            userRepository.save(user);
            healthcareServiceClient.createDoctorProfile(token);

            emailService.sendSimpleEmail(
                    user.getEmail(),
                    "Doctor Account Approved",
                    "Congratulations! Your doctor registration has been approved. You can now log in and access your doctor dashboard."
            );


            return DoctorRequestResponse.builder()
                    .doctorReqId(request.getDoctorReqId())
                    .doctorName(user.getUsername())
                    .status(request.getStatus().name())
                    .message("Doctor approved successfully")
                    .build();

        } else {
            request.setStatus(DoctorRequestStatus.REJECTED);

            // keep rejected request in DB for history
            doctorReqRepository.save(request);

            // delete user so they can register again with same username but new licence
            userRepository.delete(user);

            return DoctorRequestResponse.builder()
                    .doctorReqId(request.getDoctorReqId())
                    .doctorName(user.getUsername())
                    .status(request.getStatus().name())
                    .message("Doctor rejected and user removed")
                    .build();
        }
    }

}
