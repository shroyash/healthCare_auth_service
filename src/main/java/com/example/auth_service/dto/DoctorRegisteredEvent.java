package com.example.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorRegisteredEvent {

    private String userId;
    private String email;
    private String username;
    private String licenseUrl;

    // REQUIRED for Kafka + Jackson
    public DoctorRegisteredEvent() {
    }

    public DoctorRegisteredEvent(String userId, String email, String username, String licenseUrl) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.licenseUrl = licenseUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }
}
