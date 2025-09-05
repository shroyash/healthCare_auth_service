package com.example.auth_service.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class KeyLoader {

    private static final String PRIVATE_KEY_PATH = "src/main/resources/private.pem";
    private static final String PUBLIC_KEY_PATH = "src/main/resources/public.pem";

    @Bean
    public PrivateKey privateKey() throws Exception {
        return PemUtils.readPrivateKey(PRIVATE_KEY_PATH);
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        return PemUtils.readPublicKey(PUBLIC_KEY_PATH);
    }
}
