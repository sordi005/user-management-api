package com.sordi.userManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración CORS para permitir conexiones desde el frontend React.
 *
 * DESARROLLO LOCAL:
 * - Permite React en localhost:3000 y localhost:3001
 * - Permite todos los métodos HTTP (GET, POST, PUT, DELETE)
 * - Permite headers de autorización (JWT)
 *
 * @author Santiago Sordi
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir orígenes específicos para desarrollo
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",    // React puerto por defecto
            "http://localhost:3001",    // React puerto alternativo
            "http://127.0.0.1:3000",    // Alternativa localhost
            "http://127.0.0.1:3001"     // Alternativa localhost
        ));

        // Permitir métodos HTTP necesarios
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Permitir headers necesarios para JWT y JSON
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",    //  JWT tokens
            "Content-Type",     //  JSON requests
            "Accept"
        ));

        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
