package com.sordi.userManagement.exception;

/**
 * Excepción para errores de lógica de negocio (400)
 * Se lanza cuando hay violaciones de reglas de negocio
 * Ejemplo: "Email ya está en uso", "Usuario menor de edad", etc.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
