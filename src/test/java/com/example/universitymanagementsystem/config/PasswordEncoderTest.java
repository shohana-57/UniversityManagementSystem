package com.example.universitymanagementsystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private final PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

    // password plain text e thake na, BCrypt hash hoy
    @Test
    void passwordIsHashedNotPlainText() {
        String hash = encoder.encode("123456");

        assertNotEquals("123456", hash);
        assertTrue(hash.startsWith("$2"));
    }

    // sothik password mille, vul password mille na
    @Test
    void correctPasswordMatchesAndWrongPasswordDoesNot() {
        String hash = encoder.encode("123456");

        assertTrue(encoder.matches("123456", hash));
        assertFalse(encoder.matches("wrong-password", hash));
    }
}
