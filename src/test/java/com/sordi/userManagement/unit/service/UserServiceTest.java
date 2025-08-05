package com.sordi.userManagement.unit.service;

import com.sordi.userManagement.exception.BusinessException;
import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.mapper.UserMapper;
import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.request.UpdateUserRequest;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.service.UserService;
import com.sordi.userManagement.fixtures.UserFixtures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserService
 *
 * ESTRUCTURA: Organizado con Nested Tests para mejor organización
 * - Tests de CreateUser: Todos los escenarios para creación de usuarios
 * - Tests de UpdateUser: Todos los escenarios para actualización de usuarios
 * - Tests de DeleteUser: Todos los escenarios para eliminación de usuarios
 * - Tests de GetUser: Todos los escenarios para consulta de usuarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("🧪 Tests Unitarios de UserService")
public class UserServiceTest {

    // 🎭 MOCKS COMPARTIDOS para todas las clases de test anidadas
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    // 🎯 SISTEMA BAJO PRUEBA
    @InjectMocks
    private UserService userService;

    // 📋 DATOS DE PRUEBA COMPARTIDOS para todas las clases anidadas
    private User mockUser;
    private UserResponse mockResponse;

    /**
     * 🔧 CONFIGURACIÓN COMPARTIDA: Se ejecuta ANTES de cada test en cualquier clase anidada
     */
    @BeforeEach
    void setUp() {
        // Solo datos verdaderamente compartidos
        mockUser = UserFixtures.createBasicUser();

        mockResponse = new UserResponse();
        mockResponse.setId(1L);
        mockResponse.setUsername("johndoe");
        mockResponse.setEmail("john.doe@test.com");
    }

    // ========================================
    // TESTS DE CREAR USUARIO
    // ========================================
    @Nested
    @DisplayName("📝 Método CreateUser")
    class CreateUserTests {

        // 📋 DATOS ESPECÍFICOS para tests de CreateUser
        private CreateUserRequest validRequest;

        /**
         * 🔧 CONFIGURACIÓN ESPECÍFICA para tests de CreateUser
         */
        @BeforeEach
        void setUpCreateTests() {
            // Crear request de usuario válido
            validRequest = UserFixtures.createValidCreateUserRequest();
        }

        /**
         * 🎯 Caso Feliz: Todas las validaciones pasan, usuario creado exitosamente
         */
        @Test
        @DisplayName("✅ Debería crear usuario exitosamente cuando todos los datos son válidos")
        void deberiaCrearUsuario_CuandoLosDatosSonValidos() {
            // 🎬 PREPARAR: Configurar mocks para creación exitosa
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByDni(validRequest.getDni())).thenReturn(false);
            when(userMapper.toEntity(validRequest)).thenReturn(mockUser);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // ⚡ EJECUTAR: Ejecutar el método bajo prueba
            UserResponse result = userService.createUser(validRequest);

            // ✅ VERIFICAR: Verificar resultados e interacciones
            assertNotNull(result, "El resultado no debería ser null");
            assertEquals(mockResponse.getUsername(), result.getUsername(), "El username debería coincidir");
            assertEquals(mockResponse.getEmail(), result.getEmail(), "El email debería coincidir");

            // Verificar que todas las interacciones ocurrieron en el orden correcto
            verify(userRepository).existsByUsername(validRequest.getUsername());
            verify(userRepository).existsByEmail(validRequest.getEmail());
            verify(userRepository).existsByDni(validRequest.getDni());
            verify(passwordEncoder).encode(validRequest.getPassword());
            verify(userRepository).save(any(User.class));
            verify(userMapper).toResponse(mockUser);
        }

        /**
         * 🚨 Caso de Error: Email ya existe en la base de datos
         */
        @Test
        @DisplayName("❌ Debería lanzar BusinessException cuando el email ya existe")
        void deberiaLanzarBusinessException_CuandoElEmailYaExiste() {
            // 🎬 PREPARAR: Simular email existente
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            // ⚡ EJECUTAR Y VERIFICAR: Verificar que se lance la excepción
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(validRequest),
                "Debería lanzar BusinessException cuando el email existe"
            );

            assertEquals("Email ya esta en uso", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }

        /**
         * 🚨 Caso de Error: Username ya existe en la base de datos
         */
        @Test
        @DisplayName("❌ Debería lanzar BusinessException cuando el username ya existe")
        void deberiaLanzarBusinessException_CuandoElUsernameYaExiste() {
            // 🎬 PREPARAR: Simular username existente
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);

            // ⚡ EJECUTAR Y VERIFICAR
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(validRequest),
                "Debería lanzar BusinessException cuando el username existe"
            );

            assertEquals("Nombre de usuario ya esta en uso", exception.getMessage());
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).existsByDni(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * 🚨 Caso de Error: DNI ya existe en la base de datos
         */
        @Test
        @DisplayName("❌ Debería lanzar BusinessException cuando el DNI ya existe")
        void deberiaLanzarBusinessException_CuandoElDniYaExiste() {
            // 🎬 PREPARAR: Username y email OK, pero DNI duplicado
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByDni(validRequest.getDni())).thenReturn(true);

            // ⚡ EJECUTAR Y VERIFICAR
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(validRequest),
                "Debería lanzar BusinessException cuando el DNI existe"
            );

            assertEquals("DNI existente", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * 🚨 Caso de Error: Rol inválido proporcionado
         */
        @Test
        @DisplayName("❌ Debería lanzar BusinessException cuando el rol es inválido")
        void deberiaLanzarBusinessException_CuandoElRolEsInvalido() {
            // 🎬 PREPARAR: Crear request con rol inválido
            CreateUserRequest invalidRoleRequest = UserFixtures.createValidCreateUserRequest();
            invalidRoleRequest.setRole("ROL_INVALIDO");

            when(userRepository.existsByUsername(invalidRoleRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(invalidRoleRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByDni(invalidRoleRequest.getDni())).thenReturn(false);

            // ⚡ EJECUTAR Y VERIFICAR
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(invalidRoleRequest),
                "Debería lanzar BusinessException cuando el rol es inválido"
            );

            assertEquals("Rol inválido. Solo se permiten: USER, ADMIN", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * 🚨 Caso Límite: Parámetro request nulo
         */
        @Test
        @DisplayName("❌ Debería lanzar IllegalArgumentException cuando el request es null")
        void deberiaLanzarIllegalArgumentException_CuandoElRequestEsNull() {
            // ⚡ EJECUTAR Y VERIFICAR: Llamar con parámetro null
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(null),
                "Debería lanzar IllegalArgumentException cuando el request es null"
            );

            // Verificar que no se llamó ningún método del repository
            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).existsByDni(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    // ========================================
    // TESTS DE ACTUALIZAR USUARIO
    // ========================================
    @Nested
    @DisplayName("✏️ Método UpdateUser")
    class UpdateUserTests {

        // 📋 DATOS ESPECÍFICOS para tests de UpdateUser
        private UpdateUserRequest validUpdateRequest;
        private Long existingUserId;
        private User existingUser;
        private UserResponse updatedResponse;

        /**
         * 🔧 CONFIGURACIÓN ESPECÍFICA para tests de UpdateUser
         */
        @BeforeEach
        void setUpUpdateTests() {
            // Crear request de actualización válido
            validUpdateRequest = UserFixtures.createValidUpdateUserRequest();

            // Usuario existente que vamos a actualizar
            existingUserId = 1L;
            existingUser = UserFixtures.createBasicUser();
            existingUser.setId(existingUserId);

            // Respuesta esperada después de actualizar
            updatedResponse = new UserResponse();
            updatedResponse.setId(existingUserId);
            updatedResponse.setFirstName("Jane"); // Nombre actualizado
            updatedResponse.setLastName("Smith");  // Apellido actualizado
            updatedResponse.setEmail("jane.smith@test.com"); // Email actualizado
            updatedResponse.setUsername("johndoe"); // Username se mantiene
        }

        /**
         * 🎯 Caso Feliz: Actualización exitosa con datos válidos
         */
        @Test
        @DisplayName("✅ Debería actualizar usuario exitosamente cuando los datos son válidos")
        void deberiaActualizarUsuario_CuandoLosDatosSonValidos() {
            // 🎬 PREPARAR: Configurar mocks para actualización exitosa
            when(userRepository.findById(existingUserId)).thenReturn(java.util.Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            when(userMapper.toResponse(existingUser)).thenReturn(updatedResponse);

            // ⚡ EJECUTAR: Ejecutar el método de actualización
            UserResponse result = userService.updateUser(existingUserId, validUpdateRequest);

            // ✅ VERIFICAR: Verificar resultados e interacciones
            assertNotNull(result, "El resultado no debería ser null");
            assertEquals(updatedResponse.getFirstName(), result.getFirstName(), "El nombre debería estar actualizado");
            assertEquals(updatedResponse.getEmail(), result.getEmail(), "El email debería estar actualizado");

            // Verificar que se llamaron los métodos correctos
            verify(userRepository).findById(existingUserId);
            verify(userMapper).updateEntity(existingUser, validUpdateRequest);
            verify(userRepository).save(existingUser);
            verify(userMapper).toResponse(existingUser);
        }

        /**
         * 🚨 Caso de Error: Usuario no encontrado
         */
        @Test
        @DisplayName("❌ Debería lanzar ResourceNotFoundException cuando el usuario no existe")
        void deberiaLanzarResourceNotFoundException_CuandoElUsuarioNoExiste() {
            // 🎬 PREPARAR: Simular usuario no encontrado
            Long nonExistentUserId = 999L;
            when(userRepository.findById(nonExistentUserId)).thenReturn(java.util.Optional.empty());

            // ⚡ EJECUTAR Y VERIFICAR: Verificar que se lance la excepción
            com.sordi.userManagement.exception.ResourceNotFoundException exception = assertThrows(
                com.sordi.userManagement.exception.ResourceNotFoundException.class,
                () -> userService.updateUser(nonExistentUserId, validUpdateRequest),
                "Debería lanzar ResourceNotFoundException cuando el usuario no existe"
            );

            assertEquals("Usuario con ID " + nonExistentUserId + " no encontrado", exception.getMessage());
            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).updateEntity(any(User.class), any());
        }

        /**
         * 🚨 Caso de Error: Email duplicado en actualización
         */
        @Test
        @DisplayName("❌ Debería lanzar BusinessException cuando el email ya existe en otro usuario")
        void deberiaLanzarBusinessException_CuandoElEmailYaExisteEnOtroUsuario() {
            // 🎬 PREPARAR: Email que ya pertenece a otro usuario
            String newEmail = "existing@test.com";
            validUpdateRequest.setEmail(newEmail);

            when(userRepository.findById(existingUserId)).thenReturn(java.util.Optional.of(existingUser));
            when(userRepository.existsByEmail(newEmail)).thenReturn(true); // Email ya existe

            // ⚡ EJECUTAR Y VERIFICAR
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateUser(existingUserId, validUpdateRequest),
                "Debería lanzar BusinessException cuando el email ya existe"
            );

            assertEquals("El email ya está en uso por otro usuario", exception.getMessage());
            verify(userRepository).findById(existingUserId);
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * 🚨 Caso Límite: Request nulo
         */
        @Test
        @DisplayName("❌ Debería lanzar IllegalArgumentException cuando el request es null")
        void deberiaLanzarIllegalArgumentException_CuandoElRequestEsNull() {
            // ⚡ EJECUTAR Y VERIFICAR: Llamar con request null
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(existingUserId, null),
                "Debería lanzar IllegalArgumentException cuando el request es null"
            );

            assertEquals("ID de usuario y datos de actualización son requeridos", exception.getMessage());
            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * 🚨 Caso Límite: ID nulo
         */
        @Test
        @DisplayName("❌ Debería lanzar IllegalArgumentException cuando el ID es null")
        void deberiaLanzarIllegalArgumentException_CuandoElIdEsNull() {
            // ⚡ EJECUTAR Y VERIFICAR: Llamar con ID null
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(null, validUpdateRequest),
                "Debería lanzar IllegalArgumentException cuando el ID es null"
            );

            assertEquals("ID de usuario y datos de actualización son requeridos", exception.getMessage());
            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * 🎯 Caso Específico: Actualizar solo email (campos opcionales)
         */
        @Test
        @DisplayName("✅ Debería actualizar solo el email cuando solo se proporciona email")
        void deberiaActualizarSoloEmail_CuandoSoloSeProporcionaEmail() {
            // 🎬 PREPARAR: Request con solo email nuevo
            validUpdateRequest.setFirstName(null);
            validUpdateRequest.setLastName(null);
            validUpdateRequest.setEmail("nuevo@email.com");

            when(userRepository.findById(existingUserId)).thenReturn(java.util.Optional.of(existingUser));
            when(userRepository.existsByEmail(validUpdateRequest.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            when(userMapper.toResponse(existingUser)).thenReturn(updatedResponse);

            // ⚡ EJECUTAR
            UserResponse result = userService.updateUser(existingUserId, validUpdateRequest);

            // ✅ VERIFICAR
            assertNotNull(result, "El resultado no debería ser null");
            verify(userRepository).findById(existingUserId);
            verify(userRepository).existsByEmail(validUpdateRequest.getEmail());
            verify(userMapper).updateEntity(existingUser, validUpdateRequest);
            verify(userRepository).save(existingUser);
        }
    }

    // ========================================
    // TESTS DE ELIMINAR USUARIO (Marcadores)
    // ========================================
    @Nested
    @DisplayName("🗑️ Método DeleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("✅ Debería eliminar usuario cuando el usuario existe")
        void deberiaEliminarUsuario_CuandoElUsuarioExiste() {
            // TODO: Implementar cuando agreguemos tests de deleteUser
            // Este test se agregará cuando implementemos los tests de deleteUser
        }

        @Test
        @DisplayName("❌ Debería lanzar excepción al intentar eliminar el último admin")
        void deberiaLanzarExcepcion_AlIntentarEliminarElUltimoAdmin() {
            // TODO: Test crítico para la lógica de negocio
            // Test crítico para la lógica de negocio de protección del último admin
        }
    }

    // ========================================
    // TESTS DE OBTENER USUARIO (Marcadores)
    // ========================================
    @Nested
    @DisplayName("🔍 Métodos GetUser")
    class GetUserTests {

        @Test
        @DisplayName("✅ Debería retornar usuario cuando el ID existe")
        void deberiaRetornarUsuario_CuandoElIdExiste() {
            // TODO: Implementar tests de getUserById
        }

        @Test
        @DisplayName("❌ Debería lanzar excepción cuando el usuario no se encuentra")
        void deberiaLanzarExcepcion_CuandoElUsuarioNoSeEncuentra() {
            // TODO: Implementar tests de ResourceNotFoundException
        }
    }
}
