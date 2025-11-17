package com.example.auth_service.globalExpection;

public class RoleNotFoundExpection extends RuntimeException{
    public RoleNotFoundExpection(String message) {
        super(message);
    }
}
