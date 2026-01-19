package com.example.auth_service.repository;

import com.example.auth_service.model.DoctorRequest;
import com.example.auth_service.enums.DoctorRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorReqRepository extends JpaRepository<DoctorRequest, Long> {

    long countByStatus(DoctorRequestStatus status);

    List<DoctorRequest> findByStatus(DoctorRequestStatus status);
}

