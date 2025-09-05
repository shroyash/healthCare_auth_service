package com.example.auth_service.service;

import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequestDto;
import com.example.auth_service.dto.UserRegistrationRequest;
import com.example.auth_service.model.AppUser;

public interface AuthService {
    AppUser registerUser(UserRegistrationRequest request);
    JwtResponse loginUser(LoginRequestDto request);
}
