package com.example.auth_service.service.authServiceImp;

import com.example.auth_service.config.JwtTokenProvider;
import com.example.auth_service.dto.ForgotPasswordRequest;
import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequestDto;
import com.example.auth_service.dto.UserRegistrationRequest;
import com.example.auth_service.globalExpection.UserAlreadyExistsException;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.RoleName;
import com.example.auth_service.respository.RoleRepository;
import com.example.auth_service.respository.UserRepository;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Default role not found"));

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

}