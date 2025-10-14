package com.example.auth_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "healthcare-service",
        url = "http://localhost:8001"
)
public interface HealthcareServiceClient {

    @PostMapping("/docter-profile/create-doctor-profile")
    ResponseEntity<String> createDoctorProfile(
            @RequestHeader("Authorization") String token);


    @PostMapping("/patient-profile/create-patience-profile")
    ResponseEntity<String> createPatientProfile(
            @RequestHeader("Authorization") String token);
}