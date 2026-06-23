package com.example.auth_service.globalExpection;

import com.example.auth_service.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(UserNotFoundException e) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountLockedException(AccountLockedException e) {
        return buildResponse(e.getMessage(), HttpStatus.LOCKED);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExpiredOrInvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredOrInvalidTokenException(ExpiredOrInvalidTokenException e) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectOldPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handleIncorrectOldPassword(IncorrectOldPasswordException e) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleNotFoundExpection.class)
    public ResponseEntity<ApiResponse<Object>> handleRoleNotFoundExpection(RoleNotFoundExpection e) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailSendException(EmailSendException e) {
        return buildResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // NEW: User Already Suspended
    @ExceptionHandler(UserAlreadySuspendedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadySuspendedException(
            UserAlreadySuspendedException e) {
        return buildResponse(e.getMessage(), HttpStatus.CONFLICT);
    }


    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountSuspendedException(
            AccountSuspendedException e) {
        return buildResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception e) {
        return buildResponse(
                "Something went wrong: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(
            String message,
            HttpStatus status) {

        ApiResponse<Object> response = ApiResponse.builder()
                .status(false)
                .message(message)
                .data(null)
                .build();

        return new ResponseEntity<>(response, status);
    }
}