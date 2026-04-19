package com.example.auth_service.service;

import com.example.auth_service.model.AppUser;

public interface LoginAttemptService {

    void loginFailed(AppUser user);

    void loginSucceeded(AppUser user);

    boolean isAccountLocked(AppUser user);
}
