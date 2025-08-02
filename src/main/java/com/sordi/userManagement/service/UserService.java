package com.sordi.userManagement.service;

import com.sordi.userManagement.exception.BusinessException;
import com.sordi.userManagement.exception.ResourceNotFoundException;
import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.mapper.UserMapper;
import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.UpdateUserRequest;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestión de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crear un nuevo usuario
     * @param request datos del usuario a crear
     * @return Respuesta con los datos del usuario creado
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Iniciando creación de usuario con username: {}", request.getUsername());

        // Validaciones con logging
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

        try {
            User user = userMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            User savedUser = userRepository.save(user);

            log.info("Usuario creado exitosamente con ID: {} y username: {}",
                    savedUser.getId(), savedUser.getUsername());

            return userMapper.toResponse(savedUser);

        } catch (Exception e) {
            log.error("Error inesperado al crear usuario con username: {}. Error: {}",
                     request.getUsername(), e.getMessage(), e);
            throw new BusinessException("Error interno al crear usuario");
        }
    }
    /**
     * Actualizar un usuario existente
     * @param id ID del usuario a actualizar
     * @param request datos de actualización
     * @return usuario actualizado
     */

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Iniciando actualización de usuario con ID: {}", id);

        if (id == null || request == null) {
            throw new IllegalArgumentException("ID de usuario y datos de actualización son requeridos");
        }

        // Buscar usuario y lanzar excepción si no existe
        User userExisting = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de actualizar usuario inexistente con ID: {}", id);
                    return new ResourceNotFoundException("Usuario con ID " + id + " no encontrado");
                });

        log.debug("Usuario encontrado para actualización: {}", userExisting.getUsername());

        // Validar email único si se está cambiando
        if (request.getEmail() != null && !request.getEmail().equals(userExisting.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Intento de actualizar con email duplicado: {}", request.getEmail());
                throw new BusinessException("El email ya está en uso por otro usuario");
            }
        }
        try {
            // Actualizar usando mapper
            userMapper.updateEntity(userExisting, request);

            // Guardar cambios
            User savedUser = userRepository.save(userExisting);
            log.info("Usuario actualizado exitosamente con ID: {}", savedUser.getId());
            return userMapper.toResponse(savedUser);

        }catch (Exception e) {
            log.error("Error inesperado al actualizar usuario con ID: {}", id, e.getMessage(), e);
            throw  new RuntimeException("Error interno al actualizar usuario");
        }
    }

    /**
     * Eliminar un usuario por ID
     * @param id ID del usuario a eliminar
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Iniciando eliminación de usuario con ID: {}", id);
        if (id == null) throw new IllegalArgumentException("ID de usuario es requerido para eliminar");
        if (!userRepository.existsById(id)) {
            log.warn("Intento de eliminar usuario inexistente con ID: {}", id);
            throw new ResourceNotFoundException("Usuario con ID " + id + " no encontrado");
        }
        log.debug("Usuario encontrado para eliminación ID: {}", id);
        try{
            userRepository.deleteById(id);
            log.info("Usuario eliminado exitosamente con ID: {}", id);
        }catch (Exception e) {
            log.error("Error inesperado al eliminar usuario con ID: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error interno al eliminar usuario");
        }
    }

    /**
     * Obtener un usuario por ID
     * @param id ID del usuario a buscar
     * @return DTO de respuesta con los datos del usuario
     */
    public UserResponse getUserById(Long id) {
        log.info("Iniciando obtención de usuario con ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de obtener usuario inexistente con ID: {}", id);
                    return new ResourceNotFoundException("Usuario con ID " + id + " no encontrado");
                });

        return userMapper.toResponse(user);
    }

    /**
     * Obtener todos los usuarios con paginación básica
     * @param page número de página (inicia en 0)
     * @param size cantidad de usuarios por página
     * @return página de usuarios
     */
    public Page<UserResponse> getAllUsers(int page, int size) {
        log.info("Obteniendo usuarios - Página: {}, Tamaño: {}", page, size);
        if (page <= 0 || size <= 0)  {
            log.warn("Parámetros de paginación inválidos - Página: {}, Tamaño: {}", page, size);
            throw new BusinessException("Parámetros de paginación inválidos");
        }
        try {
            // Crear paginación ordenada por ID
            Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

            // Obtener página de usuarios de la BD
            Page<User> userPage = userRepository.findAll(pageable);

            log.info("Se encontraron {} usuarios en página {} de {}",
                    userPage.getContent().size(), page, userPage.getTotalPages());

            // Convertir a DTOs y retornar
            return userPage.map(userMapper::toResponse);

        } catch (Exception e) {
            log.error("Error al obtener usuarios: {}", e.getMessage(), e);
            throw new BusinessException("Error interno al obtener usuarios");
        }
    }

    /**
     * Buscar usuarios por nombre con paginación
     * @param firstName nombre a buscar
     * @param page número de página
     * @param size tamaño de página
     * @return página de usuarios que coincidan
     */
    public Page<UserResponse> searchUsersByName(String firstName, int page, int size) {
        log.info("Buscando usuarios por nombre: '{}' - Página: {}, Tamaño: {}", firstName, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName"));
            Page<User> userPage = userRepository.findByFirstNameContainingIgnoreCase(firstName, pageable);

            log.info("Se encontraron {} usuarios con nombre: '{}'", userPage.getContent().size(), firstName);
            return userPage.map(userMapper::toResponse);

        } catch (Exception e) {
            log.error("Error en búsqueda por nombre: {}", e.getMessage(), e);
            throw new BusinessException("Error interno en búsqueda");
        }
    }
}
