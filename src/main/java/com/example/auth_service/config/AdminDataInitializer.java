package com.example.auth_service.config;

import com.example.auth_service.enums.Gender;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.model.Role;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.enums.RoleName;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_ADMIN)));

        if (userRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .roles(Collections.singleton(adminRole))
                    .gender(Gender.MALE)
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .country("Nepal")
                    .build();

            userRepository.save(admin);
            System.out.println("Admin user created successfully!");
        } else {
            System.out.println("Admin user already exists, skipping creation.");
        }
    }
}
