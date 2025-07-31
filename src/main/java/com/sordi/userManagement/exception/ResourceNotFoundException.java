package com.sordi.userManagement.exception;

/**
 * Excepción para recursos no encontrados (404)
 * Se lanza cuando se busca un usuario, rol, etc. que no existe
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s no encontrado con %s: '%s'", resource, field, value));
    }
}
