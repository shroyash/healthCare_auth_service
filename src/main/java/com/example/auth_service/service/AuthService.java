package com.example.auth_service.service;

import com.example.auth_service.dto.UserRegistrationRequest;
import com.example.auth_service.model.UserDetails;

public interface AuthService {
    UserDetails registerUser(UserRegistrationRequest request);
    UserDetails loginUser(UserRegistrationRequest request);
}
