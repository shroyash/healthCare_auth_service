package com.example.auth_service.globalExpection;

public class UserAlreadySuspendedException extends RuntimeException {

    public UserAlreadySuspendedException(String message) {
        super(message);
    }
}