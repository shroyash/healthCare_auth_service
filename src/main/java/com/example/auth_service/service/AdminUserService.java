package com.example.auth_service.service;

import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.enums.RoleName;
import com.example.auth_service.globalExpection.RoleNotFoundExpection;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet())
                ));
    }

    public UserResponseDto getUserById(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    @Transactional
    public void deleteUser(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Transactional
    public UserResponseDto changeUserRole(UUID userId, RoleName newRole) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Role role = roleRepository.findByName(newRole)
                .orElseThrow(() -> new RoleNotFoundExpection("Role not found"));

        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet())
        );
    }
}