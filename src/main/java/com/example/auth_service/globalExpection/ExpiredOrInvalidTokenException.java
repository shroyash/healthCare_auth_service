package com.example.auth_service.globalExpection;

public class ExpiredOrInvalidTokenException extends RuntimeException {

    public ExpiredOrInvalidTokenException(String message) {
        super(message);
    }

    public ExpiredOrInvalidTokenException() {
        super("Token is invalid or has expired");
    }
}
