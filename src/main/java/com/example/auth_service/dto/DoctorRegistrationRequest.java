package com.example.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorRegistrationRequest {
        private String username;
        private String email;
        private String password;

        @JsonProperty("license")
        private String license;
}


