package com.example.auth_service.globalExpection;

public class UserNotFoundExpection extends RuntimeException{

    public UserNotFoundExpection(String message){
        super(message);
    }
}
