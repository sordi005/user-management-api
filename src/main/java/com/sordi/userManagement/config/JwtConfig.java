package com.sordi.userManagement.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Date;

/**
 * Propiedades de configuración para JWT.
 * Lee la configuración JWT desde application.yml y la proporciona a otros componentes.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
public class JwtConfig {

    /**
     * Clave secreta para firmar tokens JWT.
     * Debe tener al menos 32 caracteres por seguridad.
     */
    @NotBlank(message = "La clave secreta JWT no puede estar vacía")
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos.
     * Por defecto: 24 horas (86400000 ms)
     */
    @Positive(message = "El tiempo de expiración JWT debe ser positivo")
    private Long expiration;

    /**
     * Tiempo de expiración del refresh token en milisegundos.
     * Por defecto: 7 días (604800000 ms)
     */
    @Positive(message = "El tiempo de expiración del refresh token debe ser positivo")
    private Long refreshExpiration;

    /**
     * Valida la configuración después de que las propiedades son cargadas.
     * Asegura que los requisitos de seguridad se cumplan.
     */
    @PostConstruct
    public void validateConfig() {
        if (secret != null && secret.length() < 32) {
            log.warn("La clave JWT debe tener al menos 32 caracteres por seguridad. Longitud actual: {}", secret.length());
            throw new IllegalArgumentException(
                "La clave JWT debe tener al menos 32 caracteres por seguridad. Longitud actual: " + secret.length()
            );
        }

        if (expiration > refreshExpiration) {
            log.warn("La expiración del refresh token debe ser mayor que la del access token. " +
                     "Access Token: {} ms, Refresh Token: {} ms", expiration, refreshExpiration);
            throw new IllegalArgumentException(
                "La expiración del refresh token debe ser mayor que la del access token"
            );
        }
    }

    /**
     * Calcula la fecha de expiración para un nuevo token.
     *
     * @return Fecha cuando el token expirará
     */
    public Date calculateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration);
    }

    /**
     * Calcula la fecha de expiración para un refresh token.
     *
     * @return Fecha cuando el refresh token expirará
     */
    public Date calculateRefreshExpirationDate() {
        return new Date(System.currentTimeMillis() + refreshExpiration);
    }

    /**
     * Obtiene el tiempo de expiración en segundos (útil para JwtResponse).
     *
     * @return tiempo de expiración en segundos
     */
    public long getExpirationInSeconds() {
        return expiration / 1000;
    }
}
