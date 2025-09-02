package com.example.auth_service.service.authServiceImp;

import com.example.auth_service.model.UserDetails;
import com.example.auth_service.respository.UserRepository;
import com.example.auth_service.service.AuthService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthServiceImp implements AuthService {
    private UserRepository userRepository;
    @Override
    public void addRegister(UserDetails userDetails) {
        if(userRepository.existsByEmail(userDetails.getEmail())){

        }

    }
}
