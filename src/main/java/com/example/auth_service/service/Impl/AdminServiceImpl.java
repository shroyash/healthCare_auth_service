package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.request.DoctorRequestDto;
import com.example.auth_service.dto.response.DoctorRequestResponse;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.enums.DoctorRequestStatus;
import com.example.auth_service.enums.RoleName;
import com.example.auth_service.event.DoctorRegisteredEvent;
import com.example.auth_service.globalExpection.RoleNotFoundExpection;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.DoctorRequest;
import com.example.auth_service.model.Role;
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

import java.util.List;
import java.util.Set;
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

    // ================= USERS =================

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

    // ================= DOCTOR REQUESTS =================

    @Override
    public List<DoctorRequestDto> getAllDoctorRequests() {
        return doctorReqRepository.findAll()
                .stream()
                .map(dr -> DoctorRequestDto.builder()
                        .doctorReqId(dr.getDoctorReqId())
                        .userName(dr.getUsername())
                        .email(dr.getEmail())
                        .doctorLicence(dr.getDoctorLicence())
                        .status(dr.getStatus())
                        .build())
                .toList();
    }

    @Override
    public List<DoctorRequestDto> getPendingDoctorRequests() {
        return doctorReqRepository.findByStatus(DoctorRequestStatus.PENDING)
                .stream()
                .map(dr -> DoctorRequestDto.builder()
                        .doctorReqId(dr.getDoctorReqId())
                        .userName(dr.getUsername())
                        .email(dr.getEmail())
                        .doctorLicence(dr.getDoctorLicence())
                        .status(dr.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public DoctorRequestResponse setRejectOrAccept(long doctorReqId, boolean approve) {

        DoctorRequest request = doctorReqRepository.findById(doctorReqId)
                .orElseThrow(() -> new UserNotFoundException("Doctor request not found"));

        if (request.getStatus() != DoctorRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Doctor request already processed: " + request.getStatus()
            );
        }

        // ================= APPROVE =================
        if (approve) {
            AppUser user = new AppUser();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setDateOfBirth(request.getDateOfBirth());
            user.setGender(request.getGender());
            user.setCountry(request.getCountry());

            Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR)
                    .orElseThrow(() -> new RoleNotFoundExpection("Doctor role not found"));

            user.setRoles(Set.of(doctorRole));
            AppUser savedUser = userRepository.save(user);

            request.setStatus(DoctorRequestStatus.APPROVED);
            doctorReqRepository.save(request);

            // Kafka
            try {
                DoctorRegisteredEvent event = DoctorRegisteredEvent.builder()
                        .userId(savedUser.getId().toString())
                        .email(savedUser.getEmail())
                        .username(savedUser.getUsername())
                        .licenseUrl(request.getDoctorLicence())
                        .gender(request.getGender())
                        .country(request.getCountry())
                        .dateOfBirth(request.getDateOfBirth().toString())
                        .status(DoctorRequestStatus.APPROVED)
                        .build();

                kafkaTemplate.send("doctor-registered", event.getUserId(), event);
            } catch (Exception e) {
                log.error("Kafka doctor-approved event failed", e);
            }

            // Email
            try {
                emailService.sendSimpleEmail(
                        savedUser.getEmail(),
                        "Doctor Account Approved",
                        "Your doctor account has been approved. You can now log in."
                );
            } catch (Exception e) {
                log.error("Approval email failed", e);
            }

            return DoctorRequestResponse.builder()
                    .doctorReqId(request.getDoctorReqId())
                    .doctorName(savedUser.getUsername())
                    .status(request.getStatus().name())
                    .message("Doctor approved successfully")
                    .build();
        }

        // ================= REJECT =================
        request.setStatus(DoctorRequestStatus.REJECTED);
        doctorReqRepository.save(request);

        try {
            emailService.sendSimpleEmail(
                    request.getEmail(),
                    "Doctor Registration Rejected",
                    "Your doctor registration request was rejected."
            );
        } catch (Exception e) {
            log.error("Rejection email failed", e);
        }

        return DoctorRequestResponse.builder()
                .doctorReqId(request.getDoctorReqId())
                .doctorName(request.getUsername())
                .status(request.getStatus().name())
                .message("Doctor registration rejected")
                .build();
    }
}
