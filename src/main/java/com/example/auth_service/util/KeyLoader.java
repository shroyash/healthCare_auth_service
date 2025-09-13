package com.example.auth_service.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class KeyLoader {

    @Bean
    public PrivateKey privateKey() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/private.pem")) {
            if (is == null) throw new FileNotFoundException("private.pem not found in classpath");
            byte[] keyBytes = is.readAllBytes();
            return PemUtils.readPrivateKey(keyBytes); // You need a readPrivateKey(byte[]) method
        }
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/public.pem")) {
            if (is == null) throw new FileNotFoundException("public.pem not found in classpath");
            byte[] keyBytes = is.readAllBytes();
            return PemUtils.readPublicKey(keyBytes); // You need a readPublicKey(byte[]) method
        }
    }
}
