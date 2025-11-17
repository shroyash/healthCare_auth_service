package com.example.auth_service.globalExpection;

public class IncorrectOldPasswordException extends RuntimeException {

    public IncorrectOldPasswordException() {
        super("Old password is incorrect");
    }

    public IncorrectOldPasswordException(String message) {
        super(message);
    }
}
