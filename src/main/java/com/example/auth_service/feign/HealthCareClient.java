package com.example.auth_service.feign;

import com.example.auth_service.dto.DoctorRegistrationRequest;
import com.example.auth_service.dto.DoctorRequestResponse;
import jakarta.annotation.security.PermitAll;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "healthcare-service", url = "http://api-gateway:8001")
public interface HealthCareClient {

    @PostMapping("/healthcare/doctors/requests")
    DoctorRequestResponse createDoctor(@RequestBody DoctorRegistrationRequest doctorRegistrationRequest);
}
