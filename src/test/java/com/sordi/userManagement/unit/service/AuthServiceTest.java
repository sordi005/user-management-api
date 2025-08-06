package com.sordi.userManagement.unit.service;

import com.sordi.userManagement.config.JwtConfig;
import com.sordi.userManagement.exception.BusinessException;
import com.sordi.userManagement.model.User;
import com.sordi.userManagement.model.dto.mapper.UserMapper;
import com.sordi.userManagement.model.dto.request.LoginRequest;
import com.sordi.userManagement.model.dto.request.CreateUserRequest;
import com.sordi.userManagement.model.dto.response.JwtResponse;
import com.sordi.userManagement.model.dto.response.UserResponse;
import com.sordi.userManagement.repository.UserRepository;
import com.sordi.userManagement.security.JwtTokenProvider;
import com.sordi.userManagement.service.AuthService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService
 *
 * ESTRUCTURA:
 * - Tests de Login: Autenticación de usuarios
 * - Tests de Register: Registro de nuevos usuarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("🔐 Tests Unitarios de AuthService")
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private AuthService authService;

    // 📋 DATOS DE PRUEBA COMPARTIDOS
    private LoginRequest validLoginRequest;
    private CreateUserRequest validRegisterRequest;
    private UserResponse mockUserResponse;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        // Configuración básica compartida
        //Login
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("johndoe");
        validLoginRequest.setPassword("password123");

        // Register
        validRegisterRequest = UserFixtures.createValidCreateUserRequest();
        mockUserResponse = UserFixtures.createUserResponse();
        mockAuthentication = mock(Authentication.class);
    }

    // ========================================
    // TESTS DE LOGIN
    // ========================================
    @Nested
    @DisplayName("🔑 Método Login")
    class LoginTests {

        @Test
        @DisplayName("✅ Debería autenticar usuario exitosamente con credenciales válidas")
        void deberiaAutenticarUsuario_CuandoLasCredencialesSonValidas() {
            // 🎬 PREPARAR: Configurar mocks para login exitoso
            String expectedToken = "jwt.token.aqui";
            String username = "johndoe";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
            when(mockAuthentication.getName()).thenReturn(username); // Mock para getName()
            when(jwtTokenProvider.generateToken(username))  // Pasar username, no Authentication
                .thenReturn(expectedToken);

            // EJECUTAR: Intentar login
            JwtResponse response = authService.login(validLoginRequest);

            // VERIFICAR: Login exitoso
            assertNotNull(response, "La respuesta no debería ser null");
            assertEquals(expectedToken, response.getAccessToken(), "El token debería coincidir");
            assertEquals("Bearer", response.getTokenType(), "El tipo debería ser Bearer");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtTokenProvider).generateToken(username); // Verificar con username
        }

        @Test
        @DisplayName("❌ Debería lanzar excepción con credenciales incorrectas")
        void deberiaLanzarExcepcion_CuandoLasCredencialesSonIncorrectas() {
            // PREPARAR: Simular credenciales incorrectas
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

            // EJECUTAR Y VERIFICAR: Debe fallar la autenticación
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(validLoginRequest),
                "Debería lanzar BusinessException con credenciales incorrectas"
            );

            assertTrue(exception.getMessage().contains("Credenciales inválidas"));
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtTokenProvider, never()).generateToken(any());
        }

        @Test
        @DisplayName("❌ Debería lanzar IllegalArgumentException cuando el request es null")
        void deberiaLanzarIllegalArgumentException_CuandoElRequestEsNull() {
            // EJECUTAR Y VERIFICAR: Login con request null
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(null),
                "Debería lanzar IllegalArgumentException cuando el request es null"
            );

            assertEquals("Datos de login son requeridos", exception.getMessage());
            verify(authenticationManager, never()).authenticate(any());
            verify(jwtTokenProvider, never()).generateToken(any());
        }
    }

    // ========================================
    // TESTS DE REGISTER
    // ========================================
    @Nested
    @DisplayName("📝 Método Register")
    class RegisterTests {

        @Test
        @DisplayName("✅ Debería registrar usuario exitosamente")
        void deberiaRegistrarUsuario_CuandoLosDatosSonValidos() {
            // 🎬 PREPARAR: Configurar mock de UserService
            when(authService.register(validRegisterRequest)).thenReturn(mockUserResponse);

            // ⚡ EJECUTAR: Registrar usuario
            UserResponse result = authService.register(validRegisterRequest); // Corregido: era .re()

            // ✅ VERIFICAR: Registro exitoso
            assertNotNull(result, "El resultado no debería ser null");
            assertEquals(mockUserResponse.getUsername(), result.getUsername());
            assertEquals(mockUserResponse.getEmail(), result.getEmail());

            verify(authService).register(validRegisterRequest);
        }

        @Test
        @DisplayName("❌ Debería propagar BusinessException del UserService")
        void deberiaPropagar_BusinessExceptionDelUserService() {
            // 🎬 PREPARAR: UserService lanza excepción (ej: email duplicado)
            when(authService.register(validRegisterRequest))
                .thenThrow(new BusinessException("Email ya esta en uso"));

            // ⚡ EJECUTAR Y VERIFICAR: Debe propagar la excepción
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(validRegisterRequest), // Corregido: era userService.createUser()
                "Debería propagar BusinessException del UserService"
            );

            assertEquals("Email ya esta en uso", exception.getMessage());
            verify(authService).register(validRegisterRequest);
        }

        @Test
        @DisplayName("❌ Debería lanzar IllegalArgumentException cuando el request es null")
        void deberiaLanzarIllegalArgumentException_CuandoElRequestEsNull() {
            //  EJECUTAR Y VERIFICAR: Register con request null
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(null),
                "Debería lanzar IllegalArgumentException cuando el request es null"
            );

            assertEquals("Datos de registro son requeridos", exception.getMessage());
            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository, never()).existsByUsername(any());
            verify(userRepository, never()).existsByDni(any());
            verify(userMapper, never()).toEntity(any());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }
}
