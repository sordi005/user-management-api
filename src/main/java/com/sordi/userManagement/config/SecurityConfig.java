package com.sordi.userManagement.config;

import com.sordi.userManagement.model.User;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para la aplicación.
 * Define beans de seguridad, reglas de autenticación y autorización.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Dependencias necesarias para la configuración de seguridad
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Bean para el codificador de contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean de UserDetailsService personalizado.
     * Le dice a Spring Security cómo cargar usuarios desde la base de datos.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Buscar usuario en la base de datos
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + username));

            // UserDetails es la interfaz que Spring Security entiende
            return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Ya viene encriptada de la BD
                .authorities("USER")
                .accountExpired(false) // No expirado
                .accountLocked(false) // No bloqueado
                .credentialsExpired(false) // Credenciales no expiradas
                .disabled(false) // No deshabilitado
                .build();
        };
    }
    /**
        * Bean para el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Bean de SecurityFilterChain - Define las reglas de seguridad de la aplicación.
     *
     * ¿Qué hace?
     * - Define qué endpoints son públicos (sin autenticación)
     * - Define qué endpoints requieren autenticación
     * - Configura políticas de sesión (stateless para JWT)
     * - Deshabilita CSRF (no necesario para APIs REST)
     * - Configura CORS para frontend
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar Cross-Site Request Forgery (CSRF)
            .csrf(csrf -> csrf.disable())

            // Configurar sesiones como STATELESS (para JWT)
            // No guardamos estado en el servidor
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configurar qué endpoints requieren autenticación
            .authorizeHttpRequests(auth -> auth
                // ENDPOINTS PÚBLICOS (sin autenticación)
                .requestMatchers("/api/v1/auth/**").permitAll()        // Login, register
                .requestMatchers("/h2-console/**").permitAll()         // Base de datos H2 - solo desarrollo

                // ENDPOINTS OPCIONALES
                .requestMatchers("/swagger-ui/**").permitAll()         //  Documentación Swagger
                //.requestMatchers("/v3/api-docs/**").permitAll()        //  Datos para Swagger
                //.requestMatchers("/actuator/**").permitAll()           //  Monitoreo de aplicación

                // ENDPOINTS PRIVADOS (requieren autenticación)
                .anyRequest().authenticated()
            )

            // Configurar headers para H2 console (solo desarrollo)
            // H2 usa frames y Spring Security los bloquea por defecto
            .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }

}
