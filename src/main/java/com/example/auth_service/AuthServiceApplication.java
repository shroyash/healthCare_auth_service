package com.example.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.auth_service.repository")
public class AuthServiceApplication {
    public static void main(String[] args) {
        System.out.println("Starting application with explicit JPA repository scanning...");
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}