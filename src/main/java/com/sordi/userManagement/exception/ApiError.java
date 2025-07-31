package com.sordi.userManagement.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase estándar para respuestas de error de la API
 * Proporciona un formato consistente para todos los errores
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    /**
     * Timestamp cuando ocurrió el error
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP (400, 404, 500, etc.)
     */
    private int status;

    /**
     * Nombre del error HTTP (Bad Request, Not Found, etc.)
     */
    private String error;

    /**
     * Mensaje principal del error
     */
    private String message;

    /**
     * Lista de errores detallados (útil para validaciones)
     */
    private List<String> details;

    /**
     * Path de la URL donde ocurrió el error
     */
    private String path;

    /**
     * Constructor para errores simples sin detalles
     */
    public ApiError(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Constructor para errores con detalles múltiples
     */
    public ApiError(int status, String error, String message, List<String> details, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
        this.path = path;
    }
}
