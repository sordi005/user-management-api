package com.sordi.userManagement.service;

import com.sordi.userManagement.config.JwtConfig;
import com.sordi.userManagement.exception.BusinessException;
import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.request.LoginRequest;
import com.sordi.userManagement.model.dto.response.JwtResponse;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de autenticación - Maneja login y JWT tokens
 * Solo se encarga de autenticar usuarios existentes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;

    /**
     * Método principal de login
     * @param loginRequest credenciales del usuario
     * @return JwtResponse con el token generado
     */
    public JwtResponse login(LoginRequest loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Buscar el usuario en la BD para obtener sus datos
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    log.warn("Usuario autenticado pero no encontrado en BD: {}", loginRequest.getUsername());
                    return new RuntimeException("Error interno de autenticación");
                });

            //  Generar JWT token
            String token = jwtTokenProvider.generateToken(user.getUsername());

            log.info("Login exitoso para usuario: {}", user.getUsername());

            // Crear y retornar respuesta
            return JwtResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")             // Tipo de token
                .expiresIn(jwtConfig.getExpirationInSeconds())// Tiempo de expiración en segundos
                .issuedAt(LocalDateTime.now())   // Cuándo se emitió
                // refreshToken se puede agregar después si es necesario
                .build();

        } catch (AuthenticationException e) {
            log.warn("Credenciales inválidas para usuario: {}", loginRequest.getUsername());
            throw new BusinessException("Credenciales inválidas");
        } catch (Exception e) {
            log.error("Error inesperado durante login: {}", e.getMessage(), e);
            throw new BusinessException("Error interno del servidor");
        }
    }

    /**
     * Validar si un token JWT es válido
     * @param token JWT token a validar
     * @return true si es válido, false si no
     */
    public boolean validateToken(String token) {
        try {
            boolean isValid = jwtTokenProvider.validateToken(token);
            log.debug("Validación de token: {}", isValid ? "VÁLIDO" : "INVÁLIDO");
            return isValid;
        } catch (Exception e) {
            log.warn("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extraer username de un JWT token
     * @param token JWT token
     * @return username contenido en el token
     */
    public String getUsernameFromToken(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            log.debug("Username extraído del token: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Error al extraer username del token: {}", e.getMessage());
            throw new BusinessException("Token inválido");
        }
    }

    /**
     * Verificar si las credenciales son correctas sin generar token
     * Útil para operaciones que requieren confirmación de contraseña
     * @param username nombre de usuario
     * @param password contraseña en texto plano
     * @return true si las credenciales son válidas
     */
    public boolean verifyCredentials(String username, String password) {
        log.debug("Verificando credenciales para usuario: {}", username);

        try {
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                log.debug("Usuario no encontrado: {}", username);
                return false;
            }

            boolean isValid = passwordEncoder.matches(password, user.getPassword());
            log.debug("Verificación de credenciales para {}: {}",
                     username, isValid ? "VÁLIDA" : "INVÁLIDA");

            return isValid;

        } catch (Exception e) {
            log.error("Error al verificar credenciales: {}", e.getMessage(), e);
            return false;
        }
    }

}
