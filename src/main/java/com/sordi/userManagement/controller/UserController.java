package com.sordi.userManagement.controller;

import com.sordi.userManagement.model.dto.request.UpdateUserRequest;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController                    // Controlador REST para manejar usuarios
@RequestMapping("/api/users")     // URL base para los endpoints de usuario
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Método para obtener el perfil del usuario autenticado
     * URL: GET /api/users/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Obteniendo perfil de usuario autenticado: {}", authentication.getName());
        UserResponse user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(user);
    }

    /**
     * Método para actualizar el perfil del usuario autenticado
     * URL: PUT /api/users/me
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<UserResponse> updateCurrentUser(Authentication authentication, @RequestBody UpdateUserRequest request) {
        log.info("Actualizando perfil de usuario autenticado: {}", authentication.getName());

        // Obtener el usuario actual para conseguir su ID
        UserResponse currentUser = userService.getUserByUsername(authentication.getName());

        // Actualizar usando el ID del usuario autenticado
        UserResponse updatedUser = userService.updateUser(currentUser.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }
}
