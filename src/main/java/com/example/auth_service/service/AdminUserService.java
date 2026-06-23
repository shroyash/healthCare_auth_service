package com.example.auth_service.service;

import com.example.auth_service.dto.response.GenderCountDto;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.enums.RoleName;
import com.example.auth_service.event.UserSuspendedEvent;
import com.example.auth_service.globalExpection.RoleNotFoundExpection;
import com.example.auth_service.globalExpection.UserAlreadySuspendedException;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Qualifier("suspendKafkaTemplate")
    private final KafkaTemplate<String, UserSuspendedEvent> suspendKafkaTemplate;

    @Value("${kafka.topics.user-suspended:user-suspended}")
    private String suspendTopic;

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

    public List<GenderCountDto> getGenderCounts() {
        return userRepository.countByGender();
    }


    @Transactional
    public void suspendUser(UUID userId, String reason) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (!user.isActive()) {
            throw new UserAlreadySuspendedException("User is already suspended: " + userId);
        }

        int updated = userRepository.suspendUser(userId, LocalDateTime.now(), reason);
        if (updated == 0) {
            throw new UserAlreadySuspendedException("User was already suspended concurrently: " + userId);
        }

        publishSuspendEvent(user, reason, true);
    }


    @Transactional
    public void unsuspendUser(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (user.isActive()) {
            throw new IllegalStateException("User is not suspended: " + userId);
        }

        int updated = userRepository.unsuspendUser(userId);
        if (updated == 0) {
            throw new IllegalStateException("User was already unsuspended concurrently: " + userId);
        }

        log.info("User unsuspended: userId={}", userId);

        publishSuspendEvent(user, null, false);
    }



    private void publishSuspendEvent(AppUser user, String reason, boolean suspended) {
        String primaryRole = user.getRoles().stream()
                .map(r -> r.getName().name())
                .findFirst()
                .orElse("PATIENT");

        UserSuspendedEvent event = UserSuspendedEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(primaryRole)
                .reason(reason)
                .suspendedAt(LocalDateTime.now())
                .suspended(suspended)
                .build();

        // userId as the Kafka key ensures all events for one user land on the same partition
        // → ordering is guaranteed per user
        suspendKafkaTemplate.send(suspendTopic, user.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // Don't roll back the DB transaction — the suspend already happened.
                        // Log and alert; an outbox pattern or retry mechanism should handle this.
                        log.error("Failed to publish suspend event for userId={}: {}",
                                user.getId(), ex.getMessage(), ex);
                    } else {
                        SendResult<String, UserSuspendedEvent> sr = result;

                        log.info(
                                "Suspend event published: userId={}, topic={}, partition={}, offset={}",
                                user.getId(),
                                sr.getRecordMetadata().topic(),
                                sr.getRecordMetadata().partition(),
                                sr.getRecordMetadata().offset()
                        );
                    }
                });
    }
}