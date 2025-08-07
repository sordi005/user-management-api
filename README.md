# 👤 User Management API

## 📋 Descripción

API REST completa desarrollada con **Spring Boot 3** para la gestión de usuarios con autenticación JWT, control de acceso basado en roles y arquitectura empresarial. Sistema completo con base de datos PostgreSQL, migraciones automáticas, testing unitario y documentación API integrada.

## ✨ Funcionalidades Implementadas

### 🔐 Sistema de Autenticación Completo
- ✅ **Registro de usuarios** con validaciones robustas (email, DNI, contraseñas seguras)
- ✅ **Login JWT** con access tokens y refresh tokens
- ✅ **Control de roles** (USER/ADMIN) con autorización granular
- ✅ **Encriptación BCrypt** para contraseñas
- ✅ **Validación de tokens** en endpoints protegidos

### 👥 Gestión Completa de Usuarios (CRUD)
- ✅ **Crear usuarios** (endpoint admin)
- ✅ **Listar usuarios** con paginación (page/size)
- ✅ **Obtener usuario por ID**
- ✅ **Actualizar usuarios** 
- ✅ **Eliminar usuarios**
- ✅ **Búsqueda** por username, email, DNI únicos

### 🗄️ Base de Datos 
- ✅ **PostgreSQL 15** con Docker Compose
- ✅ **Migraciones Flyway** automáticas (V1: tabla users, V2: roles)
- ✅ **Índices optimizados** para performance
- ✅ **Constraints únicos** (username, email, DNI)
- ✅ **Datos de prueba** automáticos en desarrollo

### 🛡️ Seguridad 
- ✅ **Spring Security 6** con configuración completa
- ✅ **JWT Provider** personalizado con validación
- ✅ **CORS configurado** para frontend
- ✅ **Exception handling** centralizado
- ✅ **Validaciones Bean Validation** en todos los DTOs

### 🧪 Testing 
- ✅ **Tests unitarios** con JUnit 5 + Mockito
- ✅ **Tests organizados** por servicios (AuthService, UserService)
- ✅ **Test fixtures** reutilizables
- ✅ **Cobertura** de casos edge y validaciones

### 📚 Documentación Automática
- ✅ **Swagger UI** integrado (`/swagger-ui.html`)
- ✅ **OpenAPI 3** completo (`/v3/api-docs`)
- ✅ **Postman Collection** funcional con variables
- ✅ **Tests automáticos** en Postman

### 🛠️ DevOps y Automatización
- ✅ **Docker Compose** para PostgreSQL
- ✅ **Variables de entorno** (.env configurado)
- ✅ **Scripts automatización** (reset-db.bat, validate-env.bat)
- ✅ **Perfiles Spring** (dev/prod/test) bien separados
- ✅ **Maven profiles** con optimizaciones

## 🚀 Stack Tecnológico Completo

### Backend Core
- **Java 21** - LTS version
- **Spring Boot 3.5.4** - Framework principal
- **Spring Security 6** - Autenticación y autorización
- **Spring Data JPA** - ORM y repositorios
- **Spring Validation** - Validaciones automáticas

### Base de Datos
- **PostgreSQL 15** - Base de datos principal
- **Flyway** - Migraciones versionadas
- **HikariCP** - Pool de conexiones optimizado
- **H2** - Base de datos en memoria para tests

### Seguridad y JWT
- **JJWT 0.12.6** - Generación y validación JWT
- **BCrypt** - Encriptación de contraseñas
- **Spring Security JWT** - Filtros y providers personalizados

### Documentación y Testing
- **SpringDoc OpenAPI 2.7.0** - Swagger UI automático
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking para tests unitarios
- **Spring Boot Test** - Integration testing

### Desarrollo y Productividad
- **Lombok** - Reducción de boilerplate
- **MapStruct 1.5.5** - Mappers automáticos DTO ↔ Entity
- **Spring Boot DevTools** - Hot reload
- **Spring Boot Actuator** - Monitoreo y métricas

## 🏗️ Arquitectura del Proyecto

```
src/main/java/com/sordi/userManagement/
├── UserManagementApiApplication.java     # Clase principal
├── config/
│   ├── DevDataInitializer.java          # Datos de prueba automáticos
│   ├── JwtConfig.java                    # Configuración JWT
│   └── SecurityConfig.java               # Configuración Spring Security
├── controller/
│   ├── AuthController.java              # Endpoints autenticación (/api/auth)
│   ├── AdminController.java             # CRUD usuarios (/api/admin/users)
│   └── UserController.java              # Endpoints perfil usuario
├── service/
│   ├── AuthService.java                 # Lógica autenticación y registro
│   └── UserService.java                 # Lógica CRUD usuarios
├── repository/
│   └── UserRepository.java              # Acceso datos JPA
├── model/
│   ├── User.java                        # Entidad principal
│   ├── Role.java                        # Enum roles (USER/ADMIN)
│   └── dto/                            # DTOs request/response
│       ├── request/                     # CreateUser, Login, Update...
│       ├── response/                    # UserResponse, JwtResponse...
│       └── mapper/                      # MapStruct mappers
├── security/
│   ├── JwtTokenProvider.java           # Generación/validación JWT
│   ├── JwtAuthenticationFilter.java    # Filtro validación tokens
│   └── CustomUserDetailsService.java   # UserDetails personalizado
├── exception/
│   ├── GlobalExceptionHandler.java     # Manejo centralizado errores
│   ├── BusinessException.java          # Excepciones de negocio
│   └── ResourceNotFoundException.java   # Recursos no encontrados
└── util/
    └── [Utilidades varias]

src/main/resources/
├── application.yml                      # Configuración base
├── application-dev.yml                 # Desarrollo (PostgreSQL)
├── application-prod.yml                # Producción (optimizado)
├── application-test.yml                # Testing (H2 memoria)
└── db/migration/
    ├── V1__Create_users_table.sql      # Tabla usuarios + índices
    └── V2__add_roles.sql               # Columna roles

scripts/
├── reset-db.bat                        # Reset completo BD Docker
└── validate-env.bat                    # Validación entorno

postman/
├── User-Management-API-FIXED.postman_collection.json  # Colección completa
└── README-POSTMAN.md                   # Documentación Postman
```

## 🛠️ Configuración y Ejecución

### Prerrequisitos
- **Java 21+** (Verificar: `java -version`)
- **Docker & Docker Compose** (Base de datos automática)
- **Maven 3.6+** (Gestión dependencias)

### Variables de Entorno (.env configurado)
El proyecto incluye archivo `.env` con todas las variables necesarias:

```env
# Base de Datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=user_management
DB_USERNAME=user
DB_PASSWORD=1234

# Aplicación
SERVER_PORT=8086
SPRING_PROFILES_ACTIVE=dev

# JWT
JWT_SECRET=mi_super_secreto_jwt_key_debe_tener_minimo_32_caracteres_para_ser_seguro
JWT_EXPIRATION_MS=86400000

# Logging
LOG_LEVEL_APP=DEBUG
SHOW_SQL=true
```

### 🚀 Ejecutar el Proyecto

#### Opción 1: Inicio Rápido
```bash
# 1. Clonar repositorio
git clone https://github.com/sordi005/user-management-api.git
cd user-management-api

# 2. Copiar variables de entorno
cp .env.example .env
# Editar .env con tus valores

# 3. Levantar PostgreSQL automáticamente
docker-compose up -d postgres

# 4. Ejecutar aplicación
./mvnw spring-boot:run
```

#### Opción 2: Reset Completo (si hay problemas)
```bash
# En Windows - Reset automático BD
scripts\reset-db.bat

# Luego ejecutar aplicación
./mvnw spring-boot:run
```

#### Opción 3: Con Maven tradicional
```bash
mvn clean install
mvn spring-boot:run
```

🎉 **Aplicación lista en:** `http://localhost:8086`

## 🔑 Credenciales de Prueba Automáticas

El sistema crea automáticamente usuarios de prueba en desarrollo:

### Usuario Administrador
- **Username:** `admin`
- **Password:** `Admin123!`
- **Rol:** `ADMIN`
- **Acceso:** Todos los endpoints

### Usuario Regular
- **Username:** `user` 
- **Password:** `User123!`
- **Rol:** `USER`
- **Acceso:** Solo endpoints públicos

## 🚀 Cómo Usar la API

### 1️⃣ Registro de Nuevo Usuario
```bash
POST http://localhost:8086/api/auth/register
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "dateOfBirth": "1990-05-15",
  "email": "juan@example.com", 
  "dni": "87654321",
  "username": "juanperez",
  "password": "Password123!"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "id": 3,
    "username": "juanperez",
    "email": "juan@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "USER"
  },
  "status_code": 201
}
```

### 2️⃣ Login y Obtener JWT
```bash
POST http://localhost:8086/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin123!"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "username": "admin",
      "role": "ADMIN"
    }
  }
}
```

### 3️⃣ Listar Usuarios (Solo ADMIN)
```bash
GET http://localhost:8086/api/admin/users?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 4️⃣ Crear Usuario (Solo ADMIN)
```bash
POST http://localhost:8086/api/admin/users/create
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "firstName": "Ana",
  "lastName": "García",
  "dateOfBirth": "1985-12-10",
  "email": "ana@company.com",
  "dni": "11223344",
  "username": "anagarcia",
  "password": "Secure123!",
  "role": "USER"
}
```

## 📖 Documentación Interactiva

### Swagger UI (Recomendado)
- **URL:** `http://localhost:8086/swagger-ui.html`
- **Características:**
  - ✅ Interfaz visual para probar endpoints
  - ✅ Documentación automática generada
  - ✅ Ejemplos de request/response
  - ✅ Validaciones en tiempo real

### OpenAPI Specification
- **URL:** `http://localhost:8086/v3/api-docs`
- **Formato:** JSON estándar OpenAPI 3

### Postman Collection
- **Archivo:** `postman/User-Management-API-FIXED.postman_collection.json`
- **Incluye:** 
  - ✅ Todos los endpoints configurados
  - ✅ Variables de entorno automáticas
  - ✅ Tests de validación automáticos
  - ✅ Flujos completos de uso

## 📡 Endpoints Completos

### 🔐 Autenticación (`/api/auth`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/register` | Registrar nuevo usuario | ❌ |
| `POST` | `/login` | Iniciar sesión | ❌ |
| `POST` | `/refresh` | Renovar access token | ❌ |

### 👨‍💼 Administración (`/api/admin/users`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `GET` | `/` | Listar usuarios (paginado) | 🔒 ADMIN |
| `GET` | `/{id}` | Obtener usuario por ID | 🔒 ADMIN |
| `POST` | `/create` | Crear nuevo usuario | 🔒 ADMIN |
| `PUT` | `/{id}` | Actualizar usuario | 🔒 ADMIN |
| `DELETE` | `/{id}` | Eliminar usuario | 🔒 ADMIN |

### 📊 Monitoreo (`/actuator`)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `GET` | `/health` | Estado de la aplicación | ❌ |
| `GET` | `/info` | Información del proyecto | ❌ |
| `GET` | `/metrics` | Métricas de rendimiento | ❌ |

## 🧪 Testing

### Ejecutar Tests
```bash
# Tests unitarios completos
./mvnw test

# Tests con verbose output
./mvnw test -Dtest=AuthServiceTest

# Tests con coverage
./mvnw clean test jacoco:report
```

### Cobertura de Testing
- ✅ **AuthService** - Login, registro, validaciones
- ✅ **UserService** - CRUD completo, paginación  
- ✅ **Controllers** - Endpoints y validaciones
- ✅ **Security** - JWT tokens, autorización
- ✅ **Repository** - Queries y constraints

## 🚀 Próximas Funcionalidades en Desarrollo

### 🐳 Milestone 1: Containerización Completa
- [ ] **Dockerfile multi-stage** optimizado para producción
- [ ] **Docker Compose completo** (app + PostgreSQL + Redis)
- [ ] **Variables de entorno** para diferentes ambientes
- [ ] **Health checks** y monitoring containers

### 🌐 Milestone 2: Deploy en Producción
- [ ] **Deploy en Railway/Render** con PostgreSQL cloud
- [ ] **CI/CD con GitHub Actions** automatizado
- [ ] **URL pública** funcionando 24/7
- [ ] **SSL/HTTPS** configurado

### 📊 Milestone 3: Funcionalidades Avanzadas
- [ ] **Paginación avanzada** con filtros y ordenamiento
- [ ] **Cache con Redis** para optimización
- [ ] **Rate limiting** para protección API
- [ ] **Logs estructurados** con ELK Stack

### 🧪 Milestone 4: Testing Empresarial
- [ ] **Integration tests** con TestContainers
- [ ] **Coverage reports** automatizados (+90%)
- [ ] **Performance testing** con JMeter
- [ ] **Security testing** automatizado

---

**👨‍💻 Desarrollado por:** Santiago Sordi  
**📊 Estado:** Funcional completo - Listo para deploy  
**🎯 Objetivo:** Portfolio 
**📅 Última actualización:** Agosto 2025

## 🔧 Scripts de Utilidad

- `scripts/reset-db.bat` - Reset completo de base de datos
- `scripts/validate-env.bat` - Validación de entorno
- `docker-compose up -d postgres` - Levantar solo PostgreSQL
- `./mvnw spring-boot:run` - Ejecutar aplicación
