package com.example.auth_service.globalExpection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExpection {

    @ExceptionHandler(UserNotFoundExpection.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundExpection(UserNotFoundExpection e){
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalExpection(Exception e){
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "Something went wrong: " + e.getMessage(), // You can hide details in prod
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
