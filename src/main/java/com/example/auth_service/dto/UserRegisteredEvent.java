package com.example.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisteredEvent {

    private String userId;
    private String email;
    private String username;


    // REQUIRED for Kafka + Jackson
    public UserRegisteredEvent() {
    }

    public UserRegisteredEvent(String userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
    }


    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
}
