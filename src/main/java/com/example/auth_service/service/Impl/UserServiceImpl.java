package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDto getProfile(AppUser currentUser) {
        return new UserResponseDto(
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public UserResponseDto changeUsername(AppUser currentUser, String newUsername) {
        currentUser.setUsername(newUsername);
        userRepository.save(currentUser);

        return new UserResponseDto(
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }
}
