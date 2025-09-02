package com.example.auth_service.globalExpection;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ErrorResponseDTO {
    private String message;
    private int status;
    private LocalDateTime timeStamp;
}
