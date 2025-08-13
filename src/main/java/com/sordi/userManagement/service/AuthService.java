package com.sordi.userManagement.service;

import com.sordi.userManagement.config.JwtConfig;
import com.sordi.userManagement.exception.BusinessException;
import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.mapper.UserMapper;
import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.LoginRequest;
import com.sordi.userManagement.model.dto.response.JwtResponse;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;

    /**
     * Crear un nuevo usuario
     * @param request datos del usuario a crear
     * @return Respuesta con los datos del usuario creado
     */
    @Transactional
    public UserResponse register(CreateUserRequest request) {

        if (request == null) {
            log.error("Los datos de registro son requeridos para crear un nuevo usuario");
            throw new IllegalArgumentException("Datos de registro son requeridos");
        }

        log.info("Iniciando creación de usuario con username: {}", request.getUsername());

        // Validaciones de duplicados (lógica de negocio)
        if(userRepository.existsByUsername(request.getUsername())) {
            log.warn("Intento de registro fallido: Username '{}' ya existe", request.getUsername());
            throw new BusinessException("Nombre de usuario ya esta en uso");
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            log.warn("Intento de registro fallido: Email '{}' ya existe", request.getEmail());
            throw new BusinessException("Email ya esta en uso");
        }
        if(userRepository.existsByDni(request.getDni())) {
            log.warn("Intento de registro fallido: DNI '{}' ya existe", request.getDni());
            throw new BusinessException("DNI existente");
        }

        // Validación de rol (lógica de negocio)
        validateRole(request.getRole());

        try {
            User user = userMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            User savedUser = userRepository.save(user);

            log.info("Usuario creado exitosamente con ID: {} y username: {} con rol: {}",
                    savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

            return userMapper.toResponse(savedUser);

        } catch (Exception e) {
            log.error("Error inesperado al crear usuario con username: {}. Error: {}",
                    request.getUsername(), e.getMessage(), e);
            throw new BusinessException("Error interno al crear usuario");
        }
    }

    /**
     * Método principal de login
     * @param loginRequest credenciales del usuario
     * @return JwtResponse con el token generado
     */
    public JwtResponse login(LoginRequest loginRequest) {

        if (loginRequest == null || (loginRequest.getUsername() == null || loginRequest.getPassword() == null) ) {
            log.error("Los datos de registro son requeridos para crear un nuevo usuario");
            throw new IllegalArgumentException("Datos de login son requeridos");
        }

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

            //  Generar JWT token y refresh token
            String accessToken = jwtTokenProvider.generateToken(user.getUsername(),user.getRole().name());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            log.info("Login exitoso para usuario: {}", user.getUsername());

            // Crear y retornar respuesta
            return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")             // Tipo de token
                .expiresIn(jwtConfig.getExpirationInSeconds())// Tiempo de expiración en segundos
                .issuedAt(LocalDateTime.now())   // Cuándo se emitió
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
     * Renueva un access token usando un refresh token válido
     * @param refreshToken refresh token válido
     * @return nuevo JwtResponse con access token renovado
     */
    public JwtResponse refreshToken(String refreshToken) {
        log.info("Intento de renovación de token");

        try {
            // Validar que el refresh token sea válido
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                log.warn("Refresh token inválido o expirado");
                throw new BusinessException("Refresh token inválido o expirado");
            }

            // Extraer username del refresh token
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

            // Verificar que el usuario aún existe en la BD
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Usuario del refresh token no encontrado: {}", username);
                    return new BusinessException("Usuario no encontrado");
                });

            // Generar nuevo access token (y opcionalmente nuevo refresh token)
            String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(),user.getRole().name());
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            log.info("Token renovado exitosamente para usuario: {}", username);

            // Retornar nueva respuesta con tokens renovados
            return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpirationInSeconds())
                .issuedAt(LocalDateTime.now())
                .build();

        } catch (BusinessException e) {
            throw e; // Re-lanzar excepciones de negocio
        } catch (Exception e) {
            log.error("Error inesperado renovando token: {}", e.getMessage(), e);
            throw new BusinessException("Error interno renovando token");
        }
    }
    /**
     * Validar que el rol sea válido
     */
    private void validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new BusinessException("El rol es requerido");
        }

        if (!role.equals("USER") && !role.equals("ADMIN")) {
            log.warn("Intento de asignar rol inválido: {}", role);
            throw new BusinessException("Rol inválido. Solo se permiten: USER, ADMIN");
        }
    }
}
