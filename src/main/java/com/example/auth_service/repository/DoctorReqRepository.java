package com.example.auth_service.repository;

import com.example.auth_service.model.DoctorRequest;
import com.example.auth_service.model.DoctorRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorReqRepository extends JpaRepository<DoctorRequest,Long> {

    long countByStatus(DoctorRequestStatus status);

    @Query("SELECT dr FROM DoctorRequest dr JOIN FETCH dr.user")
    List<DoctorRequest> findAllWithUser();


    @Query("SELECT dr FROM DoctorRequest dr JOIN FETCH dr.user WHERE dr.status = :status")
    List<DoctorRequest> findByStatusWithUser(@Param("status") DoctorRequestStatus status);
}
