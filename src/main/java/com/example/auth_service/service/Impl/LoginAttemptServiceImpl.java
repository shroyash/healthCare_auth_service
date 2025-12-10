package com.example.auth_service.service.Impl;

import com.example.auth_service.model.AppUser;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_TIME_MINUTES = 15;

    @Override
    public void loginFailed(AppUser user) {
        int newAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newAttempts);

        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
        }

        userRepository.save(user);
    }

    @Override
    public void loginSucceeded(AppUser user) {
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
    }

    @Override
    public boolean isAccountLocked(AppUser user) {
        if (!user.isAccountLocked()) return false;

        if (user.getLockTime() == null) return true;

        long minutesPassed = Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes();

        if (minutesPassed >= LOCK_TIME_MINUTES) {
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            return false;
        }

        return true;
    }
}
