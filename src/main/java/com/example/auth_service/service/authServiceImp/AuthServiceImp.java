package com.example.auth_service.service.authServiceImp;

import com.example.auth_service.config.JwtTokenProvider;
import com.example.auth_service.dto.*;
import com.example.auth_service.globalExpection.*;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Service
@Slf4j
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;


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
        Role patientRole = roleRepository.findByName(RoleName.ROLE_PATIENT)
                .orElseThrow(() -> new RoleNotFoundExpection("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(patientRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }


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
    public void forgetPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        int token = (int)(Math.random() * 900000) + 100000; // 6-digit OTP
        user.setResetToken(String.valueOf(token));
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(1)); // 1 minute expiry
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Password Reset Code",
                "Your password reset code is: " + token + ". It expires in 1 minute."
        );
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

        // clear token so it can’t be reused
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