package com.example.auth_service.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "docteRequest")
public class DoctorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorReqId;

    @NotBlank
    private String doctorLicence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorRequestStatus status = DoctorRequestStatus.PENDING;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
}
