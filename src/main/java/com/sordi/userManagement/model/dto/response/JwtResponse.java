package com.sordi.userManagement.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta de autenticación JWT siguiendo estándares OAuth2 y de la industria.
 * Se usa después de operaciones exitosas de login o renovación de token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    /**
     * El token de acceso JWT
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Tipo de token (siempre "Bearer" para JWT)
     */
    @JsonProperty("token_type")
    private String tokenType = "Bearer";

    /**
     * Tiempo de expiración del token en segundos
     */
    @JsonProperty("expires_in")
    private long expiresIn;

    /**
     * Token de renovación para obtener nuevos tokens de acceso
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * Alcance/permisos del token (opcional)
     */
    private String scope;

    /**
     * Momento en que el token fue emitido
     */
    @JsonProperty("issued_at")
    private LocalDateTime issuedAt;

    /**
     * Constructor para respuesta JWT básica
     */
    public JwtResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.issuedAt = LocalDateTime.now();
    }
}
