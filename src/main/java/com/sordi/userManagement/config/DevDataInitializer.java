package com.sordi.userManagement.config;

import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Inicializador de datos para el entorno de desarrollo.
 * Crea usuarios de prueba con contraseñas correctamente encriptadas.
 * Solo se ejecuta cuando el perfil activo es 'dev'.
 *
 * @author Santiago Sordi
 * @version 1.0
 */
@Component
@Profile("dev")
@Slf4j
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        log.info("Iniciando la carga de datos de desarrollo...");

        try {
            createAdminUser();
            createRegularUser();
            log.info("Datos de desarrollo cargados exitosamente");
        } catch (Exception e) {
            log.error("Error al cargar datos de desarrollo: {}", e.getMessage());
        }
    }

    /**
     * Crea el usuario administrador de prueba si no existe.
     */
    private void createAdminUser() {
        String adminUsername = "admin";

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Usuario admin ya existe, saltando creación");
            return;
        }

        CreateUserRequest adminRequest = new CreateUserRequest(
            "Admin",
            "System",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "admin@userapi.com",
            adminUsername,
            "Admin123!",
            "ADMIN"
        );

        userService.createUser(adminRequest);
        log.info("👨‍💼 Usuario ADMIN creado: {} / Admin123!", adminUsername);
    }

    /**
     * Crea el usuario regular de prueba si no existe.
     */
    private void createRegularUser() {
        String userUsername = "user";

        if (userRepository.existsByUsername(userUsername)) {
            log.info("👤 Usuario user ya existe, saltando creación");
            return;
        }

        CreateUserRequest userRequest = new CreateUserRequest(
            "John",
            "Doe",
            LocalDate.of(1995, 5, 15),
            "87654321",
            "user@userapi.com",
            userUsername,
            "User123!",
            "USER"
        );

        userService.createUser(userRequest);
        log.info("👨‍💻 Usuario USER creado: {} / User123!", userUsername);
    }
}
