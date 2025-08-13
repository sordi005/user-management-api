package com.sordi.userManagement.controller;

import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.LoginRequest;
import com.sordi.userManagement.model.dto.request.RefreshTokenRequest;
import com.sordi.userManagement.model.dto.response.ApiResponse;
import com.sordi.userManagement.model.dto.response.JwtResponse;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.service.AuthService;
import com.sordi.userManagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autentication", description = "Endpoints para login y registro")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.authService = authService;
        logger.info("AuthController Inicializado");
    }

    /**
     * Registra un nuevo usuario en el sistema
     * @param request datos del usuario a registrar
     * @return Respuesta con los datos del usuario creado
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Iniciando registro de usuario con username: {}", request.getUsername());

        // Crear el usuario usando el servicio
        UserResponse userResponse = authService.register(request);

        logger.info("Usuario registrado exitosamente con ID: {} y username: {}",
                userResponse.getId(), userResponse.getUsername());

        // Usar el método estático para crear respuesta completa
        ApiResponse<UserResponse> response = ApiResponse.success(
            userResponse,
            "Usuario registrado exitosamente",
            HttpStatus.CREATED.value()
        );

        // Devolver HTTP 201 CREATED
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Autentíca a un usuario y retorna un JWT token
     * @param request credenciales del usuario (username/email y password)
     * @return Respuesta con el JWT token
     */
    @PostMapping("/login")
    @Operation(
            summary = "Inicia sesión y devuelve un 'ApiResponse' con Token jwt",
            description = "Recibe credenciales y responde con un token JWT válido.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.
                            ApiResponse(responseCode = "200", description = "Login exitoso"),
                    @io.swagger.v3.oas.annotations.responses.
                            ApiResponse(responseCode = "401", description = "Credenciales inválidas")
            }
    )
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Intento de login para usuario: {}", request.getUsername());

        // Autenticar usuario y generar JWT usando AuthService
        JwtResponse jwtResponse = authService.login(request);

        // Log del éxito
        logger.info("Login exitoso para usuario: {}", request.getUsername());

        // Crear respuesta exitosa con ApiResponse wrapper
        ApiResponse<JwtResponse> response = ApiResponse.success(
            jwtResponse,
            "Login exitoso",
            HttpStatus.OK.value()
        );

        // Devolver HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    /**
     * Renueva un access token usando un refresh token válido
     * @param request solicitud con el refresh token
     * @return Respuesta con nuevos tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Intento de renovación de token");

        // Renovar tokens usando AuthService
        JwtResponse jwtResponse = authService.refreshToken(request.getRefreshToken());

        logger.info("Token renovado exitosamente");

        // Crear respuesta exitosa
        ApiResponse<JwtResponse> response = ApiResponse.success(
            jwtResponse,
            "Token renovado exitosamente",
            HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }
}
