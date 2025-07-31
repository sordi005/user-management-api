package com.sordi.userManagement.exception;

import com.sordi.userManagement.model.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Captura todas las excepciones y las convierte en respuestas ApiResponse consistentes.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja errores de recursos no encontrados (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Recurso no encontrado: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            "Recurso no encontrado",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja errores de lógica de negocio (400)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Error de negocio: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            "Error de validación de negocio",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de validación de campos (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Error de validación: {}", ex.getMessage());

        List<String> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ApiResponse<List<String>> response = ApiResponse.error(
            "Los datos enviados no son válidos",
            "Errores de validación encontrados",
            HttpStatus.BAD_REQUEST.value()
        );
        response.setData(details); // Detalles de validación en data

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de configuración de JWT y otros componentes (500)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.error("Error de configuración: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            "Error de configuración del sistema",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja violaciones de integridad de BD (constraint violations)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Error de integridad de datos: {}", ex.getMessage());

        String message = "Error de integridad de datos";
        if (ex.getMessage().contains("users_email_key")) {
            message = "El email ya está en uso";
        } else if (ex.getMessage().contains("users_username_key")) {
            message = "El username ya está en uso";
        } else if (ex.getMessage().contains("users_dni_key")) {
            message = "El DNI ya está registrado";
        }

        ApiResponse<Void> response = ApiResponse.error(
            "Conflicto de datos",
            message,
            HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Maneja errores de tipos de argumentos incorrectos
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Error de tipo de argumento: {}", ex.getMessage());

        String message = String.format("El parámetro '%s' debe ser de tipo %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        ApiResponse<Void> response = ApiResponse.error(
            "Error de tipo de argumento",
            message,
            HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de JSON malformado
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("JSON malformado: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            "Error de formato JSON",
            "El JSON enviado no es válido",
            HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja todos los demás errores no específicos (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Error interno del servidor: ", ex);

        ApiResponse<Void> response = ApiResponse.error(
            "Error interno del servidor",
            "Ha ocurrido un error interno. Por favor contacte al administrador.",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
