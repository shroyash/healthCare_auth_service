package com.example.auth_service.dto;

import java.time.Instant;

public class UserRegisteredEvent {

    private String userId;
    private String email;
    private String username;
    private String licenseUrl;
    private Instant createdAt;


    public UserRegisteredEvent(String userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.createdAt = Instant.now();
    }

    public UserRegisteredEvent(String userId, String email, String username, String licenseUrl) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.licenseUrl = licenseUrl;
        this.createdAt = Instant.now();
    }


    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getLicenseUrl() { return licenseUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
