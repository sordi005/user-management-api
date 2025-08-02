-- V1: Crear tabla de usuarios
-- Autor: Santiago Sordi
-- Fecha: 2025-08-02
-- Descripción: Tabla inicial para almacenar información de usuarios

CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    username          VARCHAR(30) UNIQUE NOT NULL,
    email             VARCHAR(100) UNIQUE NOT NULL,
    password          VARCHAR(255) NOT NULL,
    dni               VARCHAR(10) UNIQUE NOT NULL,
    first_name        VARCHAR(50) NOT NULL,
    last_name         VARCHAR(50) NOT NULL,
    date_of_birth     DATE NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_dni ON users(dni);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Comentarios para documentación
COMMENT ON TABLE users IS 'Tabla principal de usuarios del sistema';
COMMENT ON COLUMN users.id IS 'Identificador único del usuario';
COMMENT ON COLUMN users.username IS 'Nombre de usuario único para login';
COMMENT ON COLUMN users.email IS 'Email único del usuario';
COMMENT ON COLUMN users.password IS 'Contraseña encriptada con BCrypt';
COMMENT ON COLUMN users.dni IS 'Documento Nacional de Identidad único';
COMMENT ON COLUMN users.first_name IS 'Nombre del usuario';
COMMENT ON COLUMN users.last_name IS 'Apellido del usuario';
COMMENT ON COLUMN users.date_of_birth IS 'Fecha de nacimiento';
COMMENT ON COLUMN users.created_at IS 'Timestamp de creación del registro';
COMMENT ON COLUMN users.updated_at IS 'Timestamp de última actualización';
