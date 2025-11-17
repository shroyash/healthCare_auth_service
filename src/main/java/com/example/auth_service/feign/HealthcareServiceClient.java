package com.example.auth_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "healthcare-service"
)
public interface HealthcareServiceClient {

    @PostMapping("/api/doctor-profiles")
    ResponseEntity<String> createDoctorProfile(
            @RequestHeader("Authorization") String token);

    @PostMapping("/api/patient-profiles")
    ResponseEntity<String> createPatientProfile(
            @RequestHeader("Authorization") String token);
}