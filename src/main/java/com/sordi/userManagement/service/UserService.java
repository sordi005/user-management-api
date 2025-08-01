package com.sordi.userManagement.service;

import com.sordi.userManagement.exception.BusinessException;
import com.sordi.userManagement.exception.ResourceNotFoundException;
import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.mapper.UserMapper;
import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.UpdateUserRequest;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j  // ← AGREGAR para logging
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Iniciando actualización de usuario con ID: {}", id);

        if (id == null || request == null) {
            throw new BusinessException("ID de usuario y datos de actualización son requeridos");
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
            throw  new BusinessException("Error interno al actualizar usuario");
        }

    }
    @Transactional
    public void deleteUser(Long id) {
        log.info("Iniciando Eliminación de usuario con ID: {}", id);
        if (id == null) throw new BusinessException("ID de usuario es requerido para eliminar");
        if (!userRepository.existsById(id)) {
            log.warn("Intento de eliminar usuario inexistente con ID: {}", id);
            throw new ResourceNotFoundException("Usuario con ID " + id + " no encontrado");
        }
        log.debug("Usuario encontrado para Eliminación ID: {}",id);
        try{
            userRepository.deleteById(id);
        }catch (Exception e) {
            log.error("Error inesperado al eliminar usuario con ID: {}", id, e.getMessage(), e);
            throw new BusinessException("Error interno al eliminar usuario");
        }

    }
    public UserResponse getUserById(Long id) {
        log.info("Iniciando obtener el usuario con ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de obtener el usuario inexistente con ID: {}", id);
                    new ResourceNotFoundException("Usuario con ID " + id + " no encontrado");
                });

        return userMapper.toResponse(user);
    }

    List<UserResponse> findAllUsers(){

    }


}
