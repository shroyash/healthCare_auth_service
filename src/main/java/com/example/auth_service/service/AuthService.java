package com.example.auth_service.service;

import com.example.auth_service.dto.*;
import com.example.auth_service.model.AppUser;

public interface AuthService {
    AppUser registerUser(UserRegistrationRequest request);
    AppUser registerDoctor(DoctorRegistrationRequest request);
    JwtResponse loginUser(LoginRequestDto request);
    void forgetPassword(ForgotPasswordRequest request);
    boolean verifyResetToken(VerifyResetTokenRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(AppUser user,ChangePasswordRequest request);
}
