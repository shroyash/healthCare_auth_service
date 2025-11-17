package com.example.auth_service.globalExpection;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String message){
        super(message);
    }

}
