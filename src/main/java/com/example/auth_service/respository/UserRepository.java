package com.example.auth_service.respository;

import com.example.auth_service.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDetails,Long> {

    boolean existsByEmail(String email);

    Optional<UserDetails> findByEmail(String email);

    Optional<UserDetails> findByUsername(String username);


}
