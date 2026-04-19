package com.example.auth_service.service;

import com.example.auth_service.dto.request.DoctorRequestDto;
import com.example.auth_service.dto.response.DoctorRequestResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DoctorRequestService {

    private final DoctorReqRepository doctorReqRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final KafkaTemplate<String, DoctorRegisteredEvent> kafkaTemplate;

    public DoctorRequestService(
            DoctorReqRepository doctorReqRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmailService emailService,
            @Qualifier("doctorKafkaTemplate")
            KafkaTemplate<String, DoctorRegisteredEvent> kafkaTemplate
    ) {
        this.doctorReqRepository = doctorReqRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.kafkaTemplate = kafkaTemplate;
    }

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

    @Transactional
    public DoctorRequestResponse approveDoctor(long doctorReqId) {
        DoctorRequest request = getValidatedPendingRequest(doctorReqId);

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

        publishKafkaEvent(savedUser, request);
        sendEmail(savedUser.getEmail(),
                "Doctor Account Approved",
                "Your doctor account has been approved. You can now log in.");

        return DoctorRequestResponse.builder()
                .doctorReqId(request.getDoctorReqId())
                .doctorName(savedUser.getUsername())
                .status(request.getStatus().name())
                .message("Doctor approved successfully")
                .build();
    }

    @Transactional
    public DoctorRequestResponse rejectDoctor(long doctorReqId) {
        DoctorRequest request = getValidatedPendingRequest(doctorReqId);

        request.setStatus(DoctorRequestStatus.REJECTED);
        doctorReqRepository.save(request);

        sendEmail(request.getEmail(),
                "Doctor Registration Rejected",
                "Your doctor registration request was rejected.");

        return DoctorRequestResponse.builder()
                .doctorReqId(request.getDoctorReqId())
                .doctorName(request.getUsername())
                .status(request.getStatus().name())
                .message("Doctor registration rejected")
                .build();
    }

    public int getPendingDoctorCount() {
        return (int) doctorReqRepository.countByStatus(DoctorRequestStatus.PENDING);
    }

    // ---- private helpers ----

    private DoctorRequest getValidatedPendingRequest(long doctorReqId) {
        DoctorRequest request = doctorReqRepository.findById(doctorReqId)
                .orElseThrow(() -> new UserNotFoundException("Doctor request not found"));

        if (request.getStatus() != DoctorRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Doctor request already processed: " + request.getStatus()
            );
        }
        return request;
    }

    private void publishKafkaEvent(AppUser savedUser, DoctorRequest request) {
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
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            emailService.sendSimpleEmail(to, subject, body);
        } catch (Exception e) {
            log.error("Email sending failed to: {}", to, e);
        }
    }
}