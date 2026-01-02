package com.example.auth_service.config;

import com.example.auth_service.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;


    public String generateToken(Authentication authentication, UUID userId) {
        AppUser user = (AppUser) authentication.getPrincipal();
        String username = user.getUsername(); // can be same as email
        String email = user.getEmail();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 360000000);

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .claim("username",username)
                .claim("email", email)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception ex) {
            log.error("Error extracting username from token: {}", ex.getMessage());
            return null;
        }
    }

    public Set<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String rolesStr = claims.get("roles", String.class);
            if (rolesStr != null && !rolesStr.isEmpty()) {
                return Arrays.stream(rolesStr.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet()); // ✅ collect to Set
            }
            return Set.of(); // empty set if no roles
        } catch (Exception ex) {
            log.error("Error extracting roles from token: {}", ex.getMessage());
            return Set.of();
        }
    }


}
