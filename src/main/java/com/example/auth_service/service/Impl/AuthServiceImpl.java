package com.example.auth_service.service.Impl;

import com.example.auth_service.config.JwtTokenProvider;
import com.example.auth_service.dto.*;
import com.example.auth_service.globalExpection.*;
import com.example.auth_service.model.*;
import com.example.auth_service.repository.DoctorReqRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.EmailService;
import com.example.auth_service.service.LoginAttemptService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DoctorReqRepository doctorReqRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;


    @Value("${upload.path:uploads/documents/}")
    private String uploadDir;

    public AuthServiceImpl(UserRepository userRepository,
                           DoctorReqRepository doctorReqRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           EmailService emailService,
                           LoginAttemptService loginAttemptService,
                           KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.doctorReqRepository = doctorReqRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
        this.loginAttemptService = loginAttemptService;
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

    @Override
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

        if(request.getEmail().equals("admins@gmail.com")){
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RoleNotFoundExpection("Admin role not found"));
            user.setRoles(Set.of(adminRole));
        } else {
            Role patientRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                    .orElseThrow(() -> new RoleNotFoundExpection("Default role not found"));
            user.setRoles(Set.of(patientRole));
        }

        AppUser savedUser = userRepository.save(user);

        // Publish Kafka event (UUID → String)
        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId().toString(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                null // no license for normal users
        );
        kafkaTemplate.send("user-registered", event.getUserId(), event);

        return savedUser;
    }


    @Override
    public AppUser registerDoctor(DoctorRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already in use");
        }

        // Save license file and get URL
        String licenseUrl = saveLicenseFile(request.getLicense());

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR)
                .orElseThrow(() -> new RoleNotFoundExpection("Doctor role not found"));
        user.setRoles(Set.of(doctorRole));

        AppUser savedUser = userRepository.save(user);

        // Save doctor request entity
        DoctorRequest doctorRequest = DoctorRequest.builder()
                .doctorLicence(licenseUrl)
                .status(DoctorRequestStatus.PENDING)
                .user(savedUser)
                .build();
        doctorReqRepository.save(doctorRequest);
        return savedUser;
    }


    private String saveLicenseFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


            return "/uploads/documents/" + filename;
        } catch (IOException e) {
            log.error("Failed to store license file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store license file", e);
        }
    }

    @Override
    public JwtResponse loginUser(LoginRequestDto request) {

        AppUser userInfo = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email does not exist"));


        if (loginAttemptService.isAccountLocked(userInfo)) {
            throw new RuntimeException("Account is locked due to multiple failed login attempts. Try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            AppUser user = (AppUser) authentication.getPrincipal();

            loginAttemptService.loginSucceeded(user);

            String jwt = jwtTokenProvider.generateToken(authentication, user.getId());

            return new JwtResponse(
                    jwt,
                    "Bearer",
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles()
            );

        } catch (Exception ex) {
            loginAttemptService.loginFailed(userInfo);
            throw new RuntimeException("Invalid credentials");
        }
    }


    @Override
    public LoginResponseDto loginUserWithCookie(LoginRequestDto request, HttpServletResponse response) {


        JwtResponse jwtResponse = loginUser(request);
        Set<String> roleList = jwtTokenProvider.getRolesFromToken(jwtResponse.getToken());

        setJwtCookie(response, jwtResponse.getToken());

        return new LoginResponseDto(
                "Login successful",
                jwtResponse.getUsername(),
                jwtResponse.getEmail(),
                jwtResponse.getRole()
        );
    }



    @Override
    public void logout(HttpServletResponse response) {
        clearJwtCookie(response);
    }

    private void setJwtCookie(HttpServletResponse response, String jwt) {
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    @Override
    public void forgetPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        int token = (int) (Math.random() * 900000) + 100000;
        user.setResetToken(String.valueOf(token));
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(1));
        userRepository.save(user);

        try {
            emailService.sendSimpleEmail(
                    user.getEmail(),
                    "Password Reset Code",
                    "Your password reset code is: " + token + ". It expires in 1 minute."
            );

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}. Reason: {}", user.getEmail(), e.getMessage());
            throw new EmailSendException("Failed to send password reset email. Please try again later.");
        }
    }

    @Override
    public boolean verifyResetToken(VerifyResetTokenRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getResetToken() == null || user.getTokenExpiry() == null) {
            return false;
        }

        if (!user.getResetToken().equals(request.getToken())) {
            return false;
        }

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getResetToken() == null || user.getTokenExpiry() == null ||
                !user.getResetToken().equals(request.getToken()) ||
                user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ExpiredOrInvalidTokenException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setTokenExpiry(null);

        userRepository.save(user);
    }

    @Override
    public void changePassword(AppUser user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IncorrectOldPasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}