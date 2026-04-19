package com.example.auth_service.service;

import com.example.auth_service.config.JwtTokenProvider;
import com.example.auth_service.dto.request.LoginRequestDto;
import com.example.auth_service.dto.response.JwtResponse;
import com.example.auth_service.dto.response.LoginResponseDto;
import com.example.auth_service.dto.response.UserResponseDto;
import com.example.auth_service.globalExpection.AccountLockedException;
import com.example.auth_service.globalExpection.UserNotFoundException;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AuthsService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    public JwtResponse loginUser(LoginRequestDto request) {
        AppUser userInfo = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email does not exist"));

        if (loginAttemptService.isAccountLocked(userInfo)) {
            throw new AccountLockedException("Account is locked due to multiple failed login attempts");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            AppUser user = (AppUser) authentication.getPrincipal();
            loginAttemptService.loginSucceeded(user);

            String jwt = jwtTokenProvider.generateToken(authentication, user.getId());

            return new JwtResponse(
                    jwt,
                    "Bearer",
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles()
            );

        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(userInfo);
            throw new UserNotFoundException("Invalid email or password");
        } catch (LockedException ex) {
            throw new AccountLockedException("Account is locked");
        }
    }

    public LoginResponseDto loginUserWithCookie(LoginRequestDto request, HttpServletResponse response) {
        JwtResponse jwtResponse = loginUser(request);
        setJwtCookie(response, jwtResponse.getToken());

        return new LoginResponseDto(
                "Login successful",
                jwtResponse.getUsername(),
                jwtResponse.getEmail(),
                jwtResponse.getRole()
        );
    }

    public void logout(HttpServletResponse response) {
        clearJwtCookie(response);
    }

    public UserResponseDto getCurrentUser(AppUser user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    private void setJwtCookie(HttpServletResponse response, String jwt) {
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}