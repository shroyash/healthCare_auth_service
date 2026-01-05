package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.*;
import com.example.auth_service.globalExpection.RoleNotFoundExpection;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.*;
import com.example.auth_service.repository.DoctorReqRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AdminService;
import com.example.auth_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorReqRepository doctorReqRepository;
    private final EmailService emailService;
    private final KafkaTemplate<String, DoctorRegisteredEvent> kafkaTemplate;

    public AdminServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            DoctorReqRepository doctorReqRepository,
            EmailService emailService,
            @Qualifier("doctorKafkaTemplate")
            KafkaTemplate<String, DoctorRegisteredEvent> kafkaTemplate
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorReqRepository = doctorReqRepository;
        this.emailService = emailService;
        this.kafkaTemplate = kafkaTemplate;
    }

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
    public UserResponseDto getUserById(UUID id) {
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
    @Transactional
    public void deleteUser(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDto changeUserRole(UUID userId, RoleName newRole) {
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

    @Override
    @Transactional
    public DoctorRequestResponse setRejectOrAccept(String token, long doctorReqId, boolean approve) {
        DoctorRequest request = doctorReqRepository.findById(doctorReqId)
                .orElseThrow(() -> new RuntimeException("Doctor request not found"));

        // Check if already processed
        if (request.getStatus() != DoctorRequestStatus.PENDING) {
            throw new RuntimeException("Doctor request has already been processed with status: " + request.getStatus());
        }

        AppUser user = request.getUser();

        if (user == null) {
            throw new RuntimeException("User not found for doctor request");
        }

        if (approve) {
            // APPROVE DOCTOR
            request.setStatus(DoctorRequestStatus.APPROVED);

            Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR)
                    .orElseThrow(() -> new RoleNotFoundExpection("Doctor role not found"));

            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }

            // Remove patient role and add doctor role
            user.getRoles().removeIf(role -> role.getName() == RoleName.ROLE_PATIENT);
            user.getRoles().add(doctorRole);

            // Save to database
            doctorReqRepository.save(request);
            userRepository.save(user);

            // Send Kafka event with ACTUAL license URL (FIXED)
            try {
                DoctorRegisteredEvent event = new DoctorRegisteredEvent(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getUsername(),
                        request.getDoctorLicence()  // ✅ FIXED: Now passing actual license URL
                );
                kafkaTemplate.send("doctor-registered", event.getUserId(), event);
                log.info("Kafka event sent for doctor approval: userId={}", user.getId());
            } catch (Exception e) {
                log.error("Failed to send Kafka event for doctor: {}", user.getId(), e);
                // Don't fail transaction if Kafka fails
            }

            // Send approval email
            try {
                emailService.sendSimpleEmail(
                        user.getEmail(),
                        "Doctor Account Approved",
                        "Congratulations! Your doctor registration has been approved. You can now log in and access your doctor dashboard."
                );
            } catch (Exception e) {
                log.error("Failed to send approval email to: {}", user.getEmail(), e);
            }

            log.info("Doctor approved successfully: userId={}, doctorReqId={}", user.getId(), request.getDoctorReqId());

            return DoctorRequestResponse.builder()
                    .doctorReqId(request.getDoctorReqId())
                    .doctorName(user.getUsername())
                    .status(request.getStatus().name())
                    .message("Doctor approved successfully")
                    .build();

        } else {
            // REJECT DOCTOR
            request.setStatus(DoctorRequestStatus.REJECTED);
            doctorReqRepository.save(request);

            // Keep user as patient (for audit trail)
            Role patientRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                    .orElseThrow(() -> new RoleNotFoundExpection("Patient role not found"));

            user.getRoles().clear();
            user.getRoles().add(patientRole);
            userRepository.save(user);

            // If you want to delete user instead, uncomment this and comment above role change:
            // userRepository.delete(user);

            // Send rejection email
            try {
                emailService.sendSimpleEmail(
                        user.getEmail(),
                        "Doctor Registration Update",
                        "We regret to inform you that your doctor registration could not be approved at this time. Please contact support for more information."
                );
            } catch (Exception e) {
                log.error("Failed to send rejection email to: {}", user.getEmail(), e);
            }

            log.info("Doctor rejected: userId={}, doctorReqId={}", user.getId(), request.getDoctorReqId());

            return DoctorRequestResponse.builder()
                    .doctorReqId(request.getDoctorReqId())
                    .doctorName(user.getUsername())
                    .status(request.getStatus().name())
                    .message("Doctor registration rejected")
                    .build();
        }
    }

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
}