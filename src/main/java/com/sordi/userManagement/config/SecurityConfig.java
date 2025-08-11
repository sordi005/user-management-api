package com.sordi.userManagement.config;

import com.sordi.userManagement.security.CustomUserDetailsService;
import com.sordi.userManagement.security.JwtAuthenticationFilter;
import com.sordi.userManagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de seguridad para la aplicación.
 * Define beans de seguridad, reglas de autenticación y autorización.
 */
@Configuration // Marca esta clase como una configuración de Spring
@EnableWebSecurity // Habilita la configuración de seguridad web
@RequiredArgsConstructor
public class SecurityConfig {

    // Dependencias necesarias para la configuración de seguridad
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Bean para el codificador de contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider,customUserDetailsService);}

    /**
     * Bean de SecurityFilterChain - Define las reglas de seguridad de la aplicación.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            // CONFIGURAR CORS PARA DESARROLLO LOCAL
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Deshabilitar Cross-Site Request Forgery (CSRF)
            .csrf(AbstractHttpConfigurer::disable)

            // Configurar sesiones como STATELESS (para JWT)
            // No guardamos estado en el servidor
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configurar qué endpoints requieren autenticación
            .authorizeHttpRequests(auth -> auth
                // ACTUATOR - ESPECÍFICOS PRIMERO (orden importante)
                .requestMatchers("/actuator/health").permitAll()        // Solo health básico público
                .requestMatchers("/api/actuator/health").permitAll()    // Solo health básico con context path
                .requestMatchers("/actuator/**").hasRole("ADMIN")       // Otros endpoints solo ADMIN
                .requestMatchers("/api/actuator/**").hasRole("ADMIN")   // Otros endpoints solo ADMIN

                // ENDPOINTS PÚBLICOS (sin autenticación)
                .requestMatchers("/auth/**").permitAll()               // Login, register (sin context-path)
                .requestMatchers("/api/auth/**").permitAll()           // Login, register (con context-path /api)
                .requestMatchers("/h2-console/**").permitAll()         // Base de datos H2 - solo desarrollo

                // ENDPOINTS OPCIONALES
                .requestMatchers("/swagger-ui/**").permitAll()          // Documentación Swagger
                .requestMatchers("/v3/api-docs/**").permitAll()         // Datos para Swagger

                // ENDPOINTS PRIVADOS CON ROLES
                .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")      // Usuarios autenticados (sin context-path)
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")  // Usuarios autenticados (con context-path /api)
                .requestMatchers("/admin/**").hasRole("ADMIN")                 // Solo administradores (sin context-path)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")             // Solo administradores (con context-path /api)

                // ENDPOINTS PRIVADOS (requieren autenticación)
                .anyRequest().authenticated()
            )

            // Configurar el filtro de autenticación JWT
            // Este filtro intercepta las solicitudes y verifica el token JWT
            // Si el token es válido, permite el acceso al recurso solicitado
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

            // Configurar headers para H2 console (solo desarrollo)
            // H2 usa frames y Spring Security los bloquea por defecto
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            );

        return http.build();
    }



}
