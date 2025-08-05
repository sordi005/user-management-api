package com.sordi.userManagement.repository;

import com.sordi.userManagement.model.Role;
import com.sordi.userManagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for User management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     *
     * @param email the email to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their username.
     *
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists with the given email.
     *
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists with the given DNI.
     *
     * @param dni the DNI to check
     * @return true if user exists, false otherwise
     */
    boolean existsByDni(String dni);

    /**
     * Check if a user exists with the given username.
     *
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find a user by email or username (useful for login).
     *
     * @param email the email to search for
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
    Optional<User> findByEmailOrUsername(@Param("email") String email, @Param("username") String username);


    /**
     * Buscar usuarios por nombre con paginación
     * @param firstName nombre a buscar
     * @param pageable configuración de paginación
     * @return página de usuarios
     */
    Page<User> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    /**
     * Buscar usuarios por email con paginación
     * @param email email a buscar
     * @param pageable configuración de paginación
     * @return página de usuarios
     */
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Contar usuarios por rol
     * @param role el rol a contar
     * @return cantidad de usuarios con ese rol
     */
    long countByRole(Role role);

    /**
     * Buscar usuarios por rol
     * @param role el rol a buscar
     * @return lista de usuarios con ese rol
     */
    java.util.List<User> findByRole(Role role);
}
