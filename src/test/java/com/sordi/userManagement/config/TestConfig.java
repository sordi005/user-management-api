package com.sordi.userManagement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración específica para tests
 * Permite personalizar beans solo para el entorno de testing
 */
@TestConfiguration
public class TestConfig {

    /**
     * PasswordEncoder para tests - configuración más rápida
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        // Configuración más rápida para tests (strength menor)
        return new BCryptPasswordEncoder(4);
    }
}
