package com.example.auth_service.globalExpection;

import com.example.auth_service.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    // Handle User Not Found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(UserNotFoundException e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountLockedException(AccountLockedException e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.LOCKED);
    }


    // Handle User Already Exists
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle Expired or Invalid Token
    @ExceptionHandler(ExpiredOrInvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredOrInvalidTokenException(ExpiredOrInvalidTokenException e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle Incorrect Old Password
    @ExceptionHandler(IncorrectOldPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handleIncorrectOldPassword(IncorrectOldPasswordException e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle Role Not Found
    @ExceptionHandler(RoleNotFoundExpection.class)
    public ResponseEntity<ApiResponse<Object>> handleRoleNotFoundExpection(RoleNotFoundExpection e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle Email Sending Errors
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailSendException(EmailSendException e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Global Exception Handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception e) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message("Something went wrong: " + e.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
