# 👤 User Management API

## 📝 Descripción
API REST para gestión de usuarios desarrollada con Spring Boot 3. Incluye autenticación JWT, operaciones CRUD completas y arquitectura profesional con manejo de excepciones.

## 🚀 Tecnologías Utilizadas
- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.4** - Framework principal
- **Spring Security** - Autenticación y autorización
- **JWT** - Tokens de autenticación
- **Spring Data JPA** - Persistencia de datos
- **PostgreSQL** - Base de datos principal
- **H2** - Base de datos para testing
- **Flyway** - Migraciones de base de datos
- **Maven** - Gestión de dependencias
- **Swagger/OpenAPI** - Documentación de API
- **Lombok** - Reducción de código boilerplate
- **MapStruct** - Mapeo de objetos

## 📋 Características
- ✅ **CRUD completo de usuarios**
- ✅ **Autenticación JWT con refresh tokens**
- ✅ **Validaciones de datos robustas**
- ✅ **Manejo global de excepciones**
- ✅ **Documentación automática con Swagger**
- ✅ **Configuración por ambientes (dev/test/prod)**
- ✅ **Migraciones de base de datos con Flyway**
- ✅ **Testing unitario e integración**

## 🏗️ Arquitectura
```
src/main/java/com/sordi/userManagement/
├── config/           # Configuraciones de seguridad y JWT
├── controller/       # Controladores REST
├── exception/        # Manejo global de excepciones
├── model/           # Entidades y DTOs
├── repository/      # Repositorios JPA
├── security/        # Filtros y proveedores JWT
├── service/         # Lógica de negocio
└── util/           # Utilidades
```

## 🔗 Endpoints Principales

### Autenticación
```http
POST /api/auth/register    # Registrar usuario
POST /api/auth/login       # Iniciar sesión
POST /api/auth/refresh     # Renovar token
```

### Gestión de Usuarios
```http
GET    /api/users          # Listar usuarios (paginado)
GET    /api/users/{id}     # Obtener usuario por ID
PUT    /api/users/{id}     # Actualizar usuario
DELETE /api/users/{id}     # Eliminar usuario
```

## 🚀 Instalación y Uso

### Prerequisitos
- Java 21
- Maven 3.6+
- PostgreSQL 12+ (para producción)

### Configuración Local
1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/user-management-api.git
cd user-management-api
```

2. **Configurar base de datos**
```bash
# Crear base de datos PostgreSQL
createdb user_management

# O usar Docker
docker run --name postgres-dev -e POSTGRES_DB=user_management -e POSTGRES_USER=user -e POSTGRES_PASSWORD=1234 -p 5432:5432 -d postgres:15
```

3. **Configurar variables de entorno** (opcional)
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=user_management
export DB_USERNAME=user
export DB_PASSWORD=1234
export JWT_SECRET=mySecretKeyForJWTTokenGeneration123456789
```

4. **Ejecutar la aplicación**
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8086`

### Docker (Recomendado)
```bash
# Levantar todo el stack
docker-compose up -d

# Solo la aplicación
docker build -t user-management-api .
docker run -p 8086:8086 user-management-api
```

## 📖 Documentación API
Una vez iniciada la aplicación, la documentación interactiva estará disponible en:
- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8086/v3/api-docs

## 🧪 Testing
```bash
# Ejecutar todos los tests
./mvnw test

# Test con cobertura
./mvnw test jacoco:report
```

## 📊 Monitoreo
- **Health Check**: http://localhost:8086/actuator/health
- **Métricas**: http://localhost:8086/actuator/metrics
- **Info**: http://localhost:8086/actuator/info

## 💡 Ejemplos de Uso

### Registrar Usuario
```json
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "dateOfBirth": "1990-03-15",
  "dni": "12345678",
  "email": "juan.perez@gmail.com",
  "username": "juanperez90",
  "password": "JuanP123!"
}
```

### Iniciar Sesión
```json
POST /api/auth/login
Content-Type: application/json

{
  "username": "juanperez90",
  "password": "JuanP123!"
}
```

### Respuesta con JWT
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "timestamp": "2024-01-15T10:30:00",
  "status": 200
}
```

## 🔧 Configuración de Desarrollo

### Perfiles Disponibles
- **dev**: Desarrollo local con PostgreSQL
- **test**: Testing con H2 en memoria  
- **prod**: Producción con configuraciones optimizadas

### Variables de Entorno
| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `DB_HOST` | Host de la base de datos | `localhost` |
| `DB_PORT` | Puerto de la base de datos | `5432` |
| `DB_NAME` | Nombre de la base de datos | `user_management` |
| `JWT_SECRET` | Clave secreta para JWT | *(debe configurarse)* |
| `SERVER_PORT` | Puerto del servidor | `8086` |

## 🤝 Contribución
1. Fork el proyecto
2. Crea tu feature branch (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push al branch (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## 📄 Licencia
Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👨‍💻 Autor
**Tu Nombre** - [GitHub](https://github.com/tu-usuario) - [LinkedIn](https://linkedin.com/in/tu-perfil)

---
⭐ ¡Si te gustó este proyecto, dale una estrella en GitHub!
