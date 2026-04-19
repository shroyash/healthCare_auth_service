package com.example.auth_service.service;

import com.example.auth_service.dto.request.DoctorRegistrationRequest;
import com.example.auth_service.dto.request.UserRegistrationRequest;
import com.example.auth_service.enums.DoctorRequestStatus;
import com.example.auth_service.enums.RoleName;
import com.example.auth_service.event.UserRegisteredEvent;
import com.example.auth_service.globalExpection.RoleNotFoundExpection;
import com.example.auth_service.globalExpection.UserAlreadyExistsException;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.DoctorRequest;
import com.example.auth_service.model.Role;
import com.example.auth_service.repository.DoctorReqRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
@Slf4j
public class RegistrationService {

    private final UserRepository userRepository;
    private final DoctorReqRepository doctorReqRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Value("${upload.path:uploads/documents/}")
    private String uploadDir;

    public RegistrationService(
            UserRepository userRepository,
            DoctorReqRepository doctorReqRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Qualifier("userKafkaTemplate")
            KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate
    ) {
        this.userRepository = userRepository;
        this.doctorReqRepository = doctorReqRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", e.getMessage());
        }
    }

    public AppUser registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already in use");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setCountry(request.getCountry());

        Role patientRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                .orElseThrow(() -> new RoleNotFoundExpection("Default role not found"));
        user.setRoles(Set.of(patientRole));

        AppUser savedUser = userRepository.save(user);

        kafkaTemplate.send("user-registered", savedUser.getId().toString(),
                UserRegisteredEvent.builder()
                        .userId(savedUser.getId().toString())
                        .email(savedUser.getEmail())
                        .username(savedUser.getUsername())
                        .gender(savedUser.getGender())
                        .dateOfBirth(savedUser.getDateOfBirth().toString())
                        .country(savedUser.getCountry())
                        .build()
        );

        return savedUser;
    }

    public void registerDoctor(DoctorRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already in use");
        }

        String licenseUrl = saveLicenseFile(request.getLicense());

        DoctorRequest doctorRequest = DoctorRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .country(request.getCountry())
                .doctorLicence(licenseUrl)
                .status(DoctorRequestStatus.PENDING)
                .build();

        doctorReqRepository.save(doctorRequest);
    }

    private String saveLicenseFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/documents/" + filename;
        } catch (IOException e) {
            log.error("Failed to store license file", e);
            throw new RuntimeException("Failed to store license file", e);
        }
    }
}