package com.sordi.userManagement.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta de autenticación JWT siguiendo estándares OAuth2 e industriales.
 * Se usa después de operaciones exitosas de login o renovación de token.
 */
@Data
@Builder
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
     * Cuándo se emitió el token
     */
    @JsonProperty("issued_at")
    private LocalDateTime issuedAt;
}
