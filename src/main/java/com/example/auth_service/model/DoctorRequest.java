package com.example.auth_service.model;


import com.example.auth_service.enums.DoctorRequestStatus;
import com.example.auth_service.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "doctor_request")
public class DoctorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorReqId;

    @NotBlank
    private String doctorLicence;

    @NotBlank
    private String username;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotBlank
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorRequestStatus status = DoctorRequestStatus.PENDING;

}
