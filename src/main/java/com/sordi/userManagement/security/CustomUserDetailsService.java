package com.sordi.userManagement.security;

import com.sordi.userManagement.model.User;
import com.sordi.userManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio personalizado para cargar detalles de usuario para Spring Security.
 *
 * Esta clase implementa UserDetailsService y es utilizada por Spring Security
 * para cargar información del usuario durante la autenticación.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su nombre de usuario para Spring Security.
     *
     * @param username el nombre de usuario a buscar
     * @return UserDetails objeto que Spring Security puede usar para autenticación
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario para autenticación: {}", username);

        // Buscar usuario en la base de datos
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("Intento de login con usuario inexistente: {}", username);
                return new UsernameNotFoundException("Usuario no encontrado: " + username);
            });

        log.debug("Usuario encontrado: {}", username);

        // Convertir nuestra entidad User a UserDetails que Spring Security entiende
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword()) // Ya viene encriptada de la BD
            .authorities("ROLE_" + user.getRole().name()) // Usar rol dinámico desde BD
            .accountExpired(false) // La cuenta no está expirada
            .accountLocked(false) // La cuenta no está bloqueada
            .credentialsExpired(false) // Las credenciales no están expiradas
            .disabled(false) // La cuenta no está deshabilitada
            .build();
    }
}
