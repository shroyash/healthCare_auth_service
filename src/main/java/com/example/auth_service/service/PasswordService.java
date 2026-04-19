package com.example.auth_service.service;

import com.example.auth_service.dto.request.ChangePasswordRequest;
import com.example.auth_service.dto.request.ForgotPasswordRequest;
import com.example.auth_service.dto.request.ResetPasswordRequest;
import com.example.auth_service.dto.request.VerifyResetTokenRequest;
import com.example.auth_service.globalExpection.ExpiredOrInvalidTokenException;
import com.example.auth_service.globalExpection.IncorrectOldPasswordException;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void forgetPassword(ForgotPasswordRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        int token = (int) (Math.random() * 900000) + 100000;
        user.setResetToken(String.valueOf(token));
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(1));
        userRepository.save(user);

        emailService.sendSimpleEmail(
                user.getEmail(),
                "Password Reset Code",
                "Your password reset code is: " + token + ". It expires in 1 minute."
        );
    }

    public boolean verifyResetToken(VerifyResetTokenRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getResetToken() == null || user.getTokenExpiry() == null) return false;
        if (!user.getResetToken().equals(request.getToken())) return false;

        return user.getTokenExpiry().isAfter(LocalDateTime.now());
    }

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

    public void changePassword(AppUser user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IncorrectOldPasswordException();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}