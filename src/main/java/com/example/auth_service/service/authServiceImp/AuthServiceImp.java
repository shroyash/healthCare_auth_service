package com.example.auth_service.service.authServiceImp;

import com.example.auth_service.dto.UserRegistrationRequest;
import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.globalExpection.UserAlreadyExistsException;
import com.example.auth_service.model.UserDetails;
import com.example.auth_service.respository.UserRepository;
import com.example.auth_service.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Convert DTO to entity
        UserDetails user = new UserDetails();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("PATIENT");

        return userRepository.save(user);
    }

    public UserDetails loginUser(UserRegistrationRequest request) {
        // 1. Find user by email
        UserDetails userDetails = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // 2. Compare raw password with hashed password
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 3. Return user details if login successful
        return userDetails;
    }
}
