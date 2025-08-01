package com.sordi.userManagement.config;

import com.sordi.userManagement.model.User;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

            // Convertir User entity a UserDetails
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

}
