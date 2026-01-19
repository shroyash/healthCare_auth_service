package com.example.auth_service.event;

import com.example.auth_service.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;


@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisteredEvent {

    private String userId;
    private String email;
    private String username;
    private Gender gender;
    private String country;
    private String dateOfBirth;

    public UserRegisteredEvent() {}

    public UserRegisteredEvent(
            String userId,
            String email,
            String username,
            Gender gender,
            String country,
            String dateOfBirth
    ) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.gender = gender;
        this.country = country;
        this.dateOfBirth = dateOfBirth;
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

    public Gender getGender() {
        return gender;
    }

    public String getCountry() {
        return country;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
}
