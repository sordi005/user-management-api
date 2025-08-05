-- Agregar columna role a la tabla users
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Crear índice para optimizar consultas por rol
CREATE INDEX idx_users_role ON users(role);

-- Comentarios para documentación
COMMENT ON COLUMN users.role IS 'Rol del usuario: ADMIN, USER';
