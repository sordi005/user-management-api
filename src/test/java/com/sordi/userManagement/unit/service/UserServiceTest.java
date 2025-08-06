package com.sordi.userManagement.unit.service;

import com.sordi.userManagement.exception.BusinessException;
import com.sordi.userManagement.exception.ResourceNotFoundException;
import com.sordi.userManagement.model.Role;
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
        private CreateUserRequest validUserRequest;

        /**
         * 🔧 CONFIGURACIÓN ESPECÍFICA para tests de CreateUser
         */
        @BeforeEach
        void setUpCreateTests() {
            // Crear request de usuario válido
            validUserRequest = UserFixtures.createValidCreateUserRequest();
        }

        /**
         * 🎯 Caso Feliz: Todas las validaciones pasan, usuario creado exitosamente
         */
        @Test
        @DisplayName("✅ Debería crear usuario exitosamente cuando todos los datos son válidos")
        void deberiaCrearUsuario_CuandoLosDatosSonValidos() {
            //  PREPARAR: Configurar mocks para creación exitosa
            // Simular que el username, email y DNI no existen
            when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByDni(validUserRequest.getDni())).thenReturn(false);
            // Simular que el usuario ya existe
            when(userMapper.toEntity(validUserRequest)).thenReturn(mockUser);
            when(passwordEncoder.encode(validUserRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            //  EJECUTAR: Ejecutar el método bajo prueba
            UserResponse response = userService.createUser(validUserRequest);

            //  VERIFICAR: Verificar resultados e interacciones
            assertNotNull(response, "El resultado no debería ser null");
            assertEquals(mockResponse.getUsername(), response.getUsername(), "El username debería coincidir");
            assertEquals(mockResponse.getEmail(), response.getEmail(), "El email debería coincidir");

            // Verificar que todas las interacciones ocurrieron en el orden correcto
            verify(userRepository).existsByUsername(validUserRequest.getUsername());
            verify(userRepository).existsByEmail(validUserRequest.getEmail());
            verify(userRepository).existsByDni(validUserRequest.getDni());
            verify(passwordEncoder).encode(validUserRequest.getPassword());
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
            when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(true);

            // ⚡ EJECUTAR Y VERIFICAR: Verificar que se lance la excepción
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.createUser(validUserRequest),
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
            when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(true);

            // ⚡ EJECUTAR Y VERIFICAR
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.createUser(validUserRequest),
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
            when(userRepository.existsByUsername(validUserRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByDni(validUserRequest.getDni())).thenReturn(true);

            // ⚡ EJECUTAR Y VERIFICAR
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.createUser(validUserRequest),
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
         * Caso de Error: Email duplicado en actualización
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
         * Caso Límite: Request nulo
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
         * Caso Límite: ID nulo
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
         * Caso Específico: Actualizar solo email (campos opcionales)
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

        private Long existingUserId;
        private User existingUser;

        private User existingUserAdmin;

        /**
         * CONFIGURACIÓN ESPECÍFICA para tests de DeleteUser
         */
        @BeforeEach
        void setUpDeleteTests() {
            // Simular que el usuario existe
            existingUser = UserFixtures.createBasicUser();
            existingUserId = existingUser.getId();
        }

        @Test
        @DisplayName("✅ Debería eliminar usuario cuando el usuario existe")
        void deberiaEliminarUsuario_CuandoElUsuarioExiste() {
            // PREPARAR: Configurar mock para usuario existente
            when(userRepository.findById(existingUserId)).thenReturn(java.util.Optional.of(existingUser));

            // ⚡ EJECUTAR: Llamar al método de eliminación
            userService.deleteUser(existingUserId);

            // VERIFICAR: Verificar que se llamaron los métodos correctos
            verify(userRepository).findById(existingUserId);
            verify(userRepository).deleteById(existingUserId);
        }

        @Test
        @DisplayName("❌ Debería lanzar ResourceNotFoundException cuando el usuario no existe")
        void deberiaLanzarResourceNotFoundException_CuandoElUsuarioNoExiste() {
            // PREPARAR: Simular usuario no encontrado
            Long nonExistentUserId = 999L;
            when(userRepository.findById(nonExistentUserId)).thenReturn(java.util.Optional.empty());

            // EJECUTAR Y VERIFICAR: Verificar que se lance la excepción
            com.sordi.userManagement.exception.ResourceNotFoundException exception = assertThrows(
                    com.sordi.userManagement.exception.ResourceNotFoundException.class,
                    () -> userService.deleteUser(nonExistentUserId),
                    "Debería lanzar ResourceNotFoundException cuando el usuario no existe"
            );

            assertEquals("Usuario con ID " + nonExistentUserId + " no encontrado", exception.getMessage());
            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("❌ Debería lanzar IllegalArgumentException cuando el ID es null")
        void deberiaLanzarIllegalArgumentException_CuandoElIdEsNull() {
            // EJECUTAR Y VERIFICAR: Llamar con ID null
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.deleteUser(null),
                    "Debería lanzar IllegalArgumentException cuando el ID es null"
            );

            assertEquals("ID de usuario es requerido para eliminar", exception.getMessage());
            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("❌ Debería lanzar excepción al intentar eliminar el último admin")
        void deberiaLanzarExcepcion_AlIntentarEliminarElUltimoAdmin() {

            // Test crítico para la lógica de negocio de protección del último admin
            existingUserAdmin = UserFixtures.createAdminUser(); // ID 2L

            when(userRepository.findById(existingUserAdmin.getId())).thenReturn(java.util.Optional.of(existingUserAdmin));
            when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L); // Simular que es el último admin

            // EJECUTAR Y VERIFICAR: Verificar que se lance la excepción
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.deleteUser(existingUserAdmin.getId()),
                    "Debería lanzar BusinessException al intentar eliminar el último admin"
            );

            assertEquals("No se puede eliminar el último administrador del sistema", exception.getMessage());

            //Verificaciones de interacciones
            verify(userRepository).findById(existingUserAdmin.getId());
            verify(userRepository).countByRole(Role.ADMIN);
            verify(userRepository, never()).deleteById(any());
        }
    }

    // ========================================
    // TESTS DE OBTENER USUARIO (Marcadores)
    // ========================================
    @Nested
    @DisplayName("🔍 Métodos GetUser")
    class GetUserTests {

        @BeforeEach
        void setUpGetTests() {
            // No necesitas configuración específica aquí
            // mockUser ya está disponible de la clase padre
        }

        @Test
        @DisplayName("✅ Debería retornar usuario cuando el ID existe")
        void deberiaRetornarUsuario_CuandoElIdExiste() {
            // PREPARAR: Configurar mocks
            when(userRepository.findById(mockUser.getId())).thenReturn(java.util.Optional.of(mockUser));
            when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

            // EJECUTAR: Llamar al método
            UserResponse response = userService.getUserById(mockUser.getId());

            // VERIFICAR: Resultados e interacciones
            assertEquals(mockResponse, response); // Orden correcto: esperado, actual
            verify(userRepository).findById(mockUser.getId()); // ✅ CORRECTO
            verify(userMapper).toResponse(mockUser);
        }

        @Test
        @DisplayName("❌ Debería lanzar excepción cuando el usuario no se encuentra")
        void deberiaLanzarExcepcion_CuandoElUsuarioNoSeEncuentra() {

            //  PREPARAR: Simular usuario no encontrado
            Long nonExistentId = 999L;
            when(userRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

            // EJECUTAR Y VERIFICAR: Debe lanzar excepción
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.getUserById(nonExistentId),
                    "Debería lanzar ResourceNotFoundException cuando el usuario no existe"
            );

            assertEquals("Usuario con ID " + nonExistentId + " no encontrado", exception.getMessage());
            verify(userRepository).findById(nonExistentId);
            verify(userMapper, never()).toResponse(any(User.class));
        }
    }
}

