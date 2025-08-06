package com.sordi.userManagement.fixtures;

import com.sordi.userManagement.model.Role;
import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.UpdateUserRequest;
import com.sordi.userManagement.model.dto.response.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Factory para crear datos de prueba reutilizables
 * Centraliza la creación de objetos de test para evitar duplicación
 */
public class UserFixtures {

    /**
     * Crear un usuario básico para tests
     */
    public static User createBasicUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@test.com");
        user.setUsername("johndoe");
        user.setPassword("$2a$10$hashedPassword");
        user.setDni("12345678");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Crear un usuario administrador para tests
     */
    public static User createAdminUser() {
        User user = createBasicUser();
        user.setId(2L);
        user.setEmail("admin@test.com");
        user.setUsername("admin");
        user.setRole(Role.ADMIN);
        return user;
    }

    /**
     * Crear un CreateUserRequest válido para tests
     */
    public static CreateUserRequest createValidCreateUserRequest() {
        return new CreateUserRequest(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "john.doe@test.com",
            "johndoe",
            "Password123!",
            "USER"
        );
    }

    /**
     * Crear un UpdateUserRequest válido para tests
     */
    public static UpdateUserRequest createValidUpdateUserRequest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@test.com");
        return request;
    }

    /**
     * Crear un CreateUserRequest con email duplicado
     */
    public static CreateUserRequest createDuplicateEmailRequest() {
        CreateUserRequest request = createValidCreateUserRequest();
        request.setUsername("differentuser");
        request.setDni("87654321");
        // Mantiene el mismo email para simular duplicado
        return request;
    }

    public static UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setEmail("john.doe@test.com");
        response.setUsername("johndoe");

        return response;
    };

}
