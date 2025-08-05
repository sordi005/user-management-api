package com.sordi.userManagement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta específica para errores de token expirado
 * Indica al cliente que debe usar el refresh token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenExpiredResponse {

    private boolean success = false;
    private String message;
    private String errorCode;
    private String action; // "REFRESH_TOKEN"
    private long timestamp;

    public static TokenExpiredResponse createRefreshRequired() {
        return TokenExpiredResponse.builder()
            .success(false)
            .message("Token de acceso expirado")
            .errorCode("TOKEN_EXPIRED")
            .action("REFRESH_TOKEN")
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
