package com.example.auth_service.repository;

import com.example.auth_service.model.DoctorRequest;
import com.example.auth_service.model.DoctorRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorReqRepository extends JpaRepository<DoctorRequest,Long> {
    List<DoctorRequest> findByStatus(DoctorRequestStatus status);
}
