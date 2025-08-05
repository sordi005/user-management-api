package com.sordi.userManagement.controller;

import com.sordi.userManagement.model.dto.request.UpdateUserRequest;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController                    // Controlador REST para manejar usuarios
@RequestMapping("/api/users")     //uRL base para los endpoints de usuario
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    
    private final UserService userService;

    /**
     * Método para obtener todos los usuarios con paginación
     * URL: GET /api/users?page=0&size=10
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        log.info("Obteniendo usuarios - Página: {}, Tamaño: {}", page, size);
        Page<UserResponse> usuarios = userService.getAllUsers(page, size);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Método para obtener un usuario por su ID
     * URL: GET /api/users/123
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Obteniendo usuario con ID: {}", id);
        UserResponse usuario = userService.getUserById(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Método para actualizar un usuario existente
     * URL: PUT /api/users/123
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        log.info("Actualizando usuario con ID: {}", id);
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Método para eliminar un usuario
     * URL: DELETE /api/users/123
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();  // HTTP 204 No Content
    }

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
}
