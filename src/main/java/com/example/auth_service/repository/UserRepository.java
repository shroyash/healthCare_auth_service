package com.example.auth_service.repository;

import com.example.auth_service.dto.response.GenderCountDto;
import com.example.auth_service.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByUsername(String username);

    @Query("SELECT r.name FROM AppUser u JOIN u.roles r WHERE u.username = :username")
    String findRoleByUsername(@Param("username") String username);

    @Query("""
            SELECT new com.example.auth_service.dto.response.GenderCountDto(
                u.gender, COUNT(u.id)
            )
            FROM AppUser u
            GROUP BY u.gender
            """)
    List<GenderCountDto> countByGender();

    @Modifying
    @Query("""
            UPDATE AppUser u
            SET u.active              = false,
                u.tokenInvalidatedAt  = :invalidatedAt,
                u.suspendReason       = :reason
            WHERE u.id = :userId
              AND u.active = true
            """)
    int suspendUser(
            @Param("userId") UUID userId,
            @Param("invalidatedAt") LocalDateTime invalidatedAt,
            @Param("reason") String reason
    );

    // Reverse of suspend — re-activates the account.
    @Modifying
    @Query("""
            UPDATE AppUser u
            SET u.active             = true,
                u.tokenInvalidatedAt = null,
                u.suspendReason      = null
            WHERE u.id = :userId
              AND u.active = false
            """)
    int unsuspendUser(@Param("userId") UUID userId);
}
