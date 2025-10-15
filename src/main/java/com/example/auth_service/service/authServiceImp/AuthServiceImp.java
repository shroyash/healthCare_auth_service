package com.example.auth_service.service.authServiceImp;

import com.example.auth_service.config.JwtTokenProvider;
import com.example.auth_service.dto.*;
import com.example.auth_service.feign.HealthcareServiceClient;
import com.example.auth_service.globalExpection.*;
import com.example.auth_service.model.*;
import com.example.auth_service.repository.DoctorReqRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
@Slf4j
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final DoctorReqRepository doctorReqRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final HealthcareServiceClient healthcareServiceClient;

    @Override
    public AppUser registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Create new user
        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default role PATIENT
        if(request.getEmail().equals("admins@gmail.com")){
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RoleNotFoundExpection("Admin role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            user.setRoles(roles);
        } else {
            Role patientRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                    .orElseThrow(() -> new RoleNotFoundExpection("Default role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(patientRole);
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    @Override
    public AppUser registerDoctor(DoctorRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default role
        Role doctorRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                .orElseThrow(() -> new RoleNotFoundExpection("default role not found"));

        user.setRoles(Set.of(doctorRole));

        AppUser savedUser = userRepository.save(user);

        DoctorRequest doctorRequest = DoctorRequest.builder()
                .doctorLicence(request.getLicense())
                .status(DoctorRequestStatus.PENDING)
                .user(savedUser)
                .build();

        doctorReqRepository.save(doctorRequest);
        return savedUser;
    }

    @Override
    public JwtResponse loginUser(LoginRequestDto request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Get authenticated user
        AppUser user = (AppUser) authentication.getPrincipal();

        // Generate JWT
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Return JWT + user info
        return new JwtResponse(
                jwt,
                "Bearer",
                user.getUsername(),
                user.getEmail()
        );
    }

    @Override
    public LoginResponseDto loginUserWithCookie(LoginRequestDto request, HttpServletResponse response) {

        JwtResponse jwtResponse = loginUser(request);

        Set<String> roleList = jwtTokenProvider.getRolesFromToken(jwtResponse.getToken());

        setJwtCookie(response, jwtResponse.getToken());
        createHealthcareProfile(jwtResponse.getToken(),roleList);

        return new LoginResponseDto(
                "Login successful",
                jwtResponse.getUsername(),
                jwtResponse.getEmail()
        );
    }


    public void createHealthcareProfile(String token, Set<String> roleList) {
        try {
            boolean isDoctor = roleList.stream()
                    .anyMatch(role -> role.equalsIgnoreCase("ROLE_DOCTOR") || role.equalsIgnoreCase("DOCTOR_ROLE"));

            if (isDoctor) {
                healthcareServiceClient.createDoctorProfile(token);
                log.info("Doctor profile creation request sent successfully.");
            } else {
                healthcareServiceClient.createPatientProfile(token);
            }

        } catch (Exception e) {
            log.error("Failed to create healthcare profile for user: {}", e.getMessage());
        }
    }


    @Override
    public void logout(HttpServletResponse response) {
        // Clear the JWT cookie
        clearJwtCookie(response);
    }

    private void setJwtCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);           // Prevents XSS attacks
        cookie.setSecure(false);            // Set to true in production (HTTPS)
        cookie.setPath("/");                // Available for entire application
        cookie.setMaxAge(60 * 60);          // 1 hour expiration

        response.addCookie(cookie);
    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);            // Match the same settings as setJwtCookie
        cookie.setPath("/");
        cookie.setMaxAge(0);                // Expire immediately

        response.addCookie(cookie);
    }

    @Override
    public void forgetPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        int token = (int) (Math.random() * 900000) + 100000; // 6-digit OTP
        user.setResetToken(String.valueOf(token));
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(1)); // 1 minute expiry
        userRepository.save(user);

        try {
            emailService.sendEmail(
                    user.getEmail(),
                    "Password Reset Code",
                    "Your password reset code is: " + token + ". It expires in 1 minute."
            );
            log.info("Password reset email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}. Reason: {}", user.getEmail(), e.getMessage());
            // Option 1: silently handle (user won't know email failed)
            // Option 2: throw a custom exception to notify user
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

        // Clear token so it can't be reused
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