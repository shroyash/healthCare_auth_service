package com.example.auth_service.repository;

import com.example.auth_service.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser,Long> {

    boolean existsByEmail(String email);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUsername(String username);

    @Query("SELECT r.name FROM AppUser u JOIN u.roles r WHERE u.username = :username")
    String findRoleByUsername(@Param("username") String username);

}
