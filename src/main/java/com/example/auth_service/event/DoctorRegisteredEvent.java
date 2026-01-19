package com.example.auth_service.event;

import com.example.auth_service.enums.Gender;
import com.example.auth_service.enums.DoctorRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorRegisteredEvent {

    private String userId;
    private String email;
    private String username;
    private String licenseUrl;
    private Gender gender;
    private String country;
    private String dateOfBirth;
    private DoctorRequestStatus status;

    public DoctorRegisteredEvent() {}

    public DoctorRegisteredEvent(
            String userId,
            String email,
            String username,
            String licenseUrl,
            Gender gender,
            String country,
            String dateOfBirth,
            DoctorRequestStatus status
    ) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.licenseUrl = licenseUrl;
        this.gender = gender;
        this.country = country;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getLicenseUrl() { return licenseUrl; }
    public Gender getGender() { return gender; }
    public String getCountry() { return country; }
    public String getDateOfBirth() { return dateOfBirth; }
    public DoctorRequestStatus getStatus() { return status; }
}
