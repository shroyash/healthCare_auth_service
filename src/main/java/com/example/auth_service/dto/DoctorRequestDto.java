package com.example.auth_service.dto;

import com.example.auth_service.model.DoctorRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRequestDto {
    private long doctorReqId;
    private String userName;
    private String email;
    private String doctorLicence;
    private DoctorRequestStatus status;
}
