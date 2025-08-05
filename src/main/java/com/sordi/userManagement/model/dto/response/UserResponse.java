package com.sordi.userManagement.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sordi.userManagement.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para la entidad User.
 * Contiene solo información segura y pública sobre un usuario.
 * Nunca expone datos sensibles como contraseñas.
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    /**
     * Nombre de usuario único
     */
    private String username;

    /**
     * Fecha de nacimiento del usuario
     * Formateada como dd/MM/yyyy en las respuestas JSON
     */
    @JsonProperty("birth_date")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;

    private String dni;

    private String email;

    private String firstName;

    private String lastName;

    /**
     * Nombre completo del usuario (campo calculado)
     */
    @JsonProperty("full_name")
    private String fullName;

    /**
     * Fecha de creación de la cuenta
     * Formateada como dd/MM/yyyy HH:mm:ss en las respuestas JSON
     */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Fecha de última actualización de la cuenta
     * Formateada como dd/MM/yyyy HH:mm:ss en las respuestas JSON
     */
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    private String role;

    /**
     * Constructor personalizado para crear una respuesta básica de usuario
     */
    public UserResponse(Long id, String username, String email, String firstName, String lastName, LocalDate birthDate, String dni) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

}
