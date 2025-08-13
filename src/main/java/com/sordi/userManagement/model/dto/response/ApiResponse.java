package com.sordi.userManagement.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * Envoltura estándar para las respuestas API de todos los endpoints REST.
 * Proporciona un formato de respuesta consistente en toda la aplicación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indica si la operación fue exitosa
     */
    private boolean success;

    /**
     * Mensaje legible por humanos que describe el resultado
     */
    private String message;

    /**
     * Datos principales de la respuesta (null en caso de errores)
     */
    private T data;

    /**
     * Código de estado HTTP
     */
    @JsonProperty("status_code")  // Nombre personalizado en el JSON
    private int statusCode;

    /**
     * Marca de tiempo cuando se creó la respuesta
     */
    @JsonProperty("time_stamp")  // Nombre personalizado en el JSON
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")  // Formato de fecha
    private LocalDateTime timestamp;

    /**
     * Detalles del error (solo presente cuando success = false)
     */
    private String error;

    // ======================
    // Métodos de fábrica estáticos
    // ======================

    /**
     * Crea una respuesta exitosa con datos
     * @param <T> Tipo de los datos
     * @param data Datos a incluir en la respuesta
     * @param message Mensaje descriptivo
     * @param statusCode Código HTTP (ej: 200)
     * @return Instancia de ApiResponse configurada
     */
    public static <T> ApiResponse<T> success(T data, String message, int statusCode) {
        return new ApiResponse<>(
                true,           // Operación exitosa
                message,        // Mensaje personalizado
                data,           // Datos de respuesta
                statusCode,     // Código HTTP
                LocalDateTime.now(),  // Timestamp actual
                null            // Sin errores
        );
    }

    /**
     * Crea una respuesta exitosa con datos y mensaje por defecto
     * @param <T> Tipo de los datos
     * @param data Datos a incluir
     * @return Instancia de ApiResponse con mensaje por defecto
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(
                data,
                "Operation completed successfully",  // Mensaje por defecto
                200  // HTTP 200 OK
        );
    }

    /**
     * Crea una respuesta exitosa sin datos (solo mensaje)
     * @param <T> Tipo genérico
     * @param message Mensaje descriptivo
     * @param statusCode Código HTTP
     * @return Instancia de ApiResponse sin datos
     */
    public static <T> ApiResponse<T> success(String message, int statusCode) {
        return new ApiResponse<>(
                true,           // Operación exitosa
                message,        // Mensaje proporcionado
                null,           // Sin datos
                statusCode,     // Código HTTP
                LocalDateTime.now(),  // Timestamp actual
                null            // Sin errores
        );
    }

    /**
     * Crea una respuesta de error
     * @param <T> Tipo genérico
     * @param message Mensaje descriptivo del error
     * @param error Detalles técnicos del error
     * @param statusCode Código HTTP de error (ej: 400, 500)
     * @return Instancia de ApiResponse configurada como error
     */
    public static <T> ApiResponse<T> error(String message, String error, int statusCode) {
        return new ApiResponse<>(
                false,          // Operación fallida
                message,        // Mensaje de error
                null,           // Sin datos
                statusCode,     // Código HTTP de error
                LocalDateTime.now(),  // Timestamp actual
                error           // Detalles del error
        );
    }

    /**
     * Crea una respuesta de error con mensaje por defecto
     * @param <T> Tipo genérico
     * @param error Detalles técnicos del error
     * @param statusCode Código HTTP de error
     * @return Instancia de ApiResponse con mensaje por defecto
     */
    public static <T> ApiResponse<T> error(String error, int statusCode) {
        return error(
                "Operation failed",  // Mensaje por defecto
                error,              // Detalles del error
                statusCode          // Código HTTP
        );
    }
}

