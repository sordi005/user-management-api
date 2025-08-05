-- Agregar columna role a la tabla users
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Crear índice para optimizar consultas por rol
CREATE INDEX idx_users_role ON users(role);

-- Insertar usuarios de prueba
INSERT INTO users (first_name, last_name, date_of_birth, dni, email, username, password, role, created_at, updated_at) VALUES
-- Admin user (password: Admin123!)
('Admin', 'Sistema', '1990-01-01', '11111111', 'admin@sistema.com', 'admin', 'Example123?', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Regular user (password: User123!)
('Juan', 'Pérez', '1992-05-15', '22222222', 'juan.perez@email.com', 'juan', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Another user (password: User123!)
('María', 'García', '1995-08-20', '33333333', 'maria.garcia@email.com', 'maria', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Comentarios para documentación
COMMENT ON COLUMN users.role IS 'Rol del usuario: ADMIN, USER';
