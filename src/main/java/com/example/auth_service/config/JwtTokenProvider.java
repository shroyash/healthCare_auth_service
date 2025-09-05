package com.example.auth_service.config;

import com.example.auth_service.model.AppUser;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private final long jwtExpirationMs = 3600000; // 1 hour

    public JwtTokenProvider(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /** Generating JWT token using RSA private key */
    public String generateToken(Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256) // ✅ RSA private key
                .compact();
    }


    /** Validate JWT token using RSA public key with detailed logging */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token expired for user {}: {}", ex.getClaims().getSubject(), ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.warn("Invalid JWT token structure: {}", ex.getMessage());
        } catch (SignatureException ex) {
            logger.warn("Invalid JWT signature: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("JWT token is null or empty: {}", ex.getMessage());
        }
        return false;
    }

}
