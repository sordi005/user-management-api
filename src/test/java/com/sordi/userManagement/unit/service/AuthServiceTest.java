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

import java.util.Optional;

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

    @Mock
    private User mockUser; // ← Agregar @Mock aquí

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

        // mockUser = UserFixtures.createBasicUser(); ← Eliminar esta línea

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
            // PREPARAR: Configurar mocks para login exitoso
            String expectedToken = "jwt.token.aqui";
            String expectedRefreshToken = "jwt.token.refresh.aqui";
            String username = "johndoe";

            // Mock para AuthenticationManager
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

            // Mock para UserRepository - CRÍTICO: debe devolver el usuario
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
            when(mockUser.getUsername()).thenReturn(username); // Esto es lo que realmente usa el código

            // Mock para JwtTokenProvider
            when(jwtTokenProvider.generateToken(username)).thenReturn(expectedToken);
            when(jwtTokenProvider.generateRefreshToken(username)).thenReturn(expectedRefreshToken);

            // Mock para JwtConfig
            when(jwtConfig.getExpirationInSeconds()).thenReturn(3600L);

            // EJECUTAR: Intentar login
            JwtResponse response = authService.login(validLoginRequest);

            // VERIFICAR: Login exitoso
            assertNotNull(response, "La respuesta no debería ser null");
            assertEquals(expectedToken, response.getAccessToken(), "El access token debería coincidir");
            assertEquals(expectedRefreshToken, response.getRefreshToken(), "El refresh token debería coincidir");
            assertEquals("Bearer", response.getTokenType(), "El tipo debería ser Bearer");
            assertEquals(3600L, response.getExpiresIn(), "La expiración debería coincidir");
            assertNotNull(response.getIssuedAt(), "Debería tener fecha de emisión");

            // VERIFICAR: Que se llamaron los métodos correctos
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByUsername(username);
            verify(jwtTokenProvider).generateToken(username);
            verify(jwtTokenProvider).generateRefreshToken(username);
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
        /**
         * Caso de éxito: registro exitoso
         */
        @Test
        @DisplayName("✅ Debería registrar usuario exitosamente")
        void deberiaRegistrarUsuario_CuandoLosDatosSonValidos() {
            //  PREPARAR: Configurar mock de UserService
            when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByDni(validRegisterRequest.getDni())).thenReturn(false);

            when(userMapper.toEntity(validRegisterRequest)).thenReturn(mockUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            //  EJECUTAR: Registrar usuario
            UserResponse result = authService.register(validRegisterRequest);

            //  VERIFICAR: Registro exitoso
            assertNotNull(result, "El resultado no debería ser null");
            assertEquals(mockUserResponse.getUsername(), result.getUsername());
            assertEquals(mockUserResponse.getEmail(), result.getEmail());

            verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
            verify(userRepository).existsByUsername(validRegisterRequest.getUsername());
            verify(userRepository).existsByDni(validRegisterRequest.getDni());

            verify(userMapper).toEntity(validRegisterRequest);
            verify(passwordEncoder).encode(validRegisterRequest.getPassword());
            verify(userRepository).save(mockUser);
            verify(userMapper).toResponse(mockUser);

        }

        /**
         * Caso de error: email ya existe
         */
        @Test
        @DisplayName("❌ Debería lanzar BusinessException cuando email ya existe")
        void deberiaLanzarBusinessException_CuandoEmailYaExiste() {

            // PREPARAR: Simular que el email ya existe
            when(userRepository.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(true);

            // EJECUTAR Y VERIFICAR: Debe lanzar excepción
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(validRegisterRequest),
                "Debería lanzar BusinessException cuando email ya existe"
            );

            assertEquals("Email ya esta en uso", exception.getMessage());
            verify(userRepository).existsByUsername(any());
            verify(userRepository).existsByEmail(any());
            // Verificar que NO se llamaron otros métodos después de la validación
            verify(userRepository, never()).existsByDni(any());
        }

        /**
         * Caso de error: request null
         */
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
