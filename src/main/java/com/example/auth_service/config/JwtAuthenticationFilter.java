package com.example.auth_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.auth_service.service.CustomUserDetailsService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        logger.info("=== JWT Filter Debug ===");
        logger.info("Request URI: {}", requestURI);
        logger.info("Authorization Header: {}", authHeader != null ? "Present" : "Missing");

        String token = null;
        String tokenSource = null;

        // Try to get token from Authorization header first (for API clients)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            tokenSource = "Authorization Header";
            logger.info("Token extracted from Authorization header");
        }
        // If no header token, try to get from cookie (for web clients)
        else {
            token = getTokenFromCookie(request);
            if (token != null) {
                tokenSource = "Cookie";
                logger.info("Token extracted from cookie");
            }
        }

        // Process token if found
        if (token != null) {
            logger.info("Token source: {}", tokenSource);

            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getUsernameFromToken(token);
                    logger.info("Token valid for email: {}", email);

                    // *** KEY CHANGE: Get roles from TOKEN, not database ***
                    Set<String> rolesFromToken = jwtTokenProvider.getRolesFromToken(token);
                    logger.info("Roles from token: {}", rolesFromToken);

                    // Convert roles to GrantedAuthorities with ROLE_ prefix
                    List<GrantedAuthority> authorities = rolesFromToken.stream()
                            .map(role -> {
                                String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                                logger.info("Adding authority: {}", authorityName);
                                return new SimpleGrantedAuthority(authorityName);
                            })
                            .collect(Collectors.toList());

                    logger.info("Final authorities: {}", authorities);

                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // *** KEY CHANGE: Use authorities from TOKEN ***
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities  // NOT userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("Authentication set successfully via {}", tokenSource);
                    logger.info("User: {}, Authorities: {}", email, authorities);
                } else {
                    logger.warn("Invalid JWT token from {}", tokenSource);
                }
            } catch (Exception ex) {
                logger.error("Error processing JWT token: {}", ex.getMessage());
            }
        } else {
            logger.warn("No JWT token found in header or cookies");
        }

        // Log current authentication context
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            logger.info("Current authenticated user: {}", auth.getName());
            logger.info("Current authorities: {}", auth.getAuthorities());
        } else {
            logger.warn("No authentication found in security context");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from cookies
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    logger.info("JWT cookie found");
                    return cookie.getValue();
                }
            }
        }
        logger.info("No JWT cookie found");
        return null;
    }
}