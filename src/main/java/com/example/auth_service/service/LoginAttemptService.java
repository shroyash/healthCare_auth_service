package com.example.auth_service.service;

import com.example.auth_service.model.AppUser;

public interface LoginAttemptService {

    /**
     * Increment failed attempts and lock account if MAX_FAILED_ATTEMPTS reached
     */
    void loginFailed(AppUser user);

    /**
     * Reset failed attempts on successful login
     */
    void loginSucceeded(AppUser user);

    /**
     * Check if account is locked and unlock if lock duration passed
     * @return true if still locked, false if unlocked
     */
    boolean isAccountLocked(AppUser user);
}
