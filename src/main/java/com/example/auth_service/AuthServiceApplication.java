package com.example.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.auth_service.repository")
@EnableFeignClients(basePackages = "com.example.auth_service.feign")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class AuthServiceApplication {
    public static void main(String[] args) {
        System.out.println("Starting application with explicit JPA repository scanning...");
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}