package com.sordi.userManagement.controller;

import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.LoginRequest;
import com.sordi.userManagement.model.dto.response.ApiResponse;
import com.sordi.userManagement.model.dto.response.JwtResponse;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.service.AuthService;
import com.sordi.userManagement.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
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
        UserResponse newUser = userService.registerUser(request);

        // Log del éxito
        logger.info("Usuario registrado exitosamente con ID: {} y username: {}",
                   newUser.getId(), newUser.getUsername());

        // Usar el método estático para crear respuesta completa
        ApiResponse<UserResponse> response = ApiResponse.success(
            newUser,
            "Usuario registrado exitosamente",
            HttpStatus.CREATED.value()
        );

        // Devolver HTTP 201 CREATED
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Autentica a un usuario y retorna un JWT token
     * @param request credenciales del usuario (username/email y password)
     * @return Respuesta con el JWT token
     */
    @PostMapping("/login")
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
}
