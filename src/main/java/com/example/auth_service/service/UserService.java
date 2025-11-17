package com.example.auth_service.service;

import com.example.auth_service.dto.UserResponseDto;
import com.example.auth_service.model.AppUser;

public interface UserService {
    UserResponseDto getProfile(AppUser currentUser);
    UserResponseDto changeUsername(AppUser currentUser, String newUsername);
}
