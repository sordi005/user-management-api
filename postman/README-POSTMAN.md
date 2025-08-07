# 📮 **TESTING CON POSTMAN - User Management API**

## 🎯 **¿Qué es esta colección?**

Esta colección de Postman contiene **todos los endpoints** de la User Management API con ejemplos reales, validaciones automáticas y manejo de tokens JWT.

## 🚀 **Instalación y Uso**

### **PASO 1: Importar la Colección**
1. Abrir **Postman**
2. Click en **"Import"**
3. Seleccionar el archivo: `postman/User-Management-API-Complete.postman_collection.json`
4. ✅ ¡Listo! Ya tienes 16 tests configurados

### **PASO 2: Levantar la API**
```bash
# Desde la raíz del proyecto
docker-compose up -d

# O ejecutar directamente
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### **PASO 3: Ejecutar Tests**

#### **🔐 AUTENTICACIÓN (OBLIGATORIO PRIMERO):**
1. **"2. Login as Admin"** ← Ejecutar PRIMERO
   - Genera token de administrador automáticamente
   - Se guarda en variable `{{admin_token}}`

2. **"1. Register New User"** ← Ejecutar SEGUNDO
   - Registra usuario de prueba
   - Genera token de usuario automáticamente

#### **👤 OPERACIONES DE USUARIO:**
- ✅ Ver mi perfil
- ✅ Actualizar mi información

#### **👨‍💼 OPERACIONES DE ADMIN:**
- ✅ Ver todos los usuarios
- ✅ Crear nuevos usuarios
- ✅ Actualizar cualquier usuario
- ✅ Eliminar usuarios

## 📊 **Funcionalidades Incluidas**

### **🔄 TOKENS AUTOMÁTICOS**
```javascript
// Los tokens se guardan automáticamente
if (response.token) {
    pm.collectionVariables.set('admin_token', response.token);
}
```

### **✅ VALIDACIONES AUTOMÁTICAS**
```javascript
pm.test('✅ Usuario registrado exitosamente', function () {
    pm.response.to.have.status(200);
});
```

### **📝 LOGS INFORMATIVOS**
```javascript
console.log('🎯 Token de usuario guardado:', token.substring(0, 20) + '...');
```

## 🎯 **Casos de Uso por Funcionalidad**

### **🔐 AUTENTICACIÓN JWT**
| Endpoint | Método | Descripción | Test |
|----------|--------|-------------|------|
| `/api/auth/register` | POST | Registrar nuevo usuario | ✅ |
| `/api/auth/login` | POST | Login y obtener JWT | ✅ |
| `/api/auth/refresh` | POST | Renovar token | ✅ |

### **👤 GESTIÓN DE USUARIOS**
| Endpoint | Método | Rol Requerido | Descripción |
|----------|--------|---------------|-------------|
| `/api/users/me` | GET | USER/ADMIN | Ver mi perfil |
| `/api/users/me` | PUT | USER/ADMIN | Actualizar mi perfil |
| `/api/users` | GET | ADMIN | Ver todos los usuarios |
| `/api/users/{id}` | GET | ADMIN | Ver usuario específico |
| `/api/users` | POST | ADMIN | Crear usuario |
| `/api/users/{id}` | PUT | ADMIN | Actualizar usuario |
| `/api/users/{id}` | DELETE | ADMIN | Eliminar usuario |

### **🔍 MANEJO DE ERRORES**
| Escenario | Status Code | Test Incluido |
|-----------|-------------|---------------|
| Sin token | 401 | ✅ |
| Credenciales inválidas | 401 | ✅ |
| Usuario no encontrado | 404 | ✅ |
| Acceso denegado | 403 | ✅ |

## 📋 **Datos de Prueba Incluidos**

### **ADMIN POR DEFECTO:**
```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

### **USUARIO POR DEFECTO:**
```json
{
  "username": "user", 
  "password": "User123!"
}
```

### **USUARIO DE PRUEBA PARA REGISTRO:**
```json
{
  "username": "testuser",
  "email": "test@example.com", 
  "password": "Password123!",
  "firstName": "Test",
  "lastName": "User"
}
```

## 🔄 **USUARIOS CREADOS AUTOMÁTICAMENTE**

Tu API crea automáticamente estos usuarios al iniciar en modo `dev`:

| Usuario | Contraseña | Rol | Email |
|---------|------------|-----|-------|
| `admin` | `Admin123!` | ADMIN | admin@userapi.com |
| `user` | `User123!` | USER | user@userapi.com |

**📝 Nota:** Estos usuarios se crean automáticamente gracias al `DevDataInitializer.java`

## 🚀 **Workflow Recomendado**

### **PARA DESARROLLO:**
1. Ejecutar **"Login as Admin"**
2. Ejecutar **"Get All Users"** (ver usuarios existentes)
3. Ejecutar **"Register New User"** (crear usuario de prueba)
4. Probar funcionalidades específicas

### **PARA TESTING:**
1. Ejecutar toda la carpeta **"🔐 Authentication"**
2. Ejecutar toda la carpeta **"👤 User Operations"**
3. Ejecutar toda la carpeta **"👨‍💼 Admin Operations"**
4. Verificar **"🔍 Error Handling Tests"**

### **PARA DEMO:**
1. **"Health Check"** - Mostrar que API funciona
2. **"Login as Admin"** - Autenticación
3. **"Get All Users"** - Funcionalidad CRUD
4. **"Register New User"** - Registro
5. **"Error Handling"** - Validaciones

## 🔧 **Variables de Entorno**

La colección usa estas variables automáticamente:

```javascript
{
  "base_url": "http://localhost:8080",
  "admin_token": "", // Se llena automáticamente
  "user_token": ""   // Se llena automáticamente
}
```

## 📈 **Próximos Pasos**

### **PARA PRODUCCIÓN:**
- Cambiar `base_url` a URL de producción
- Configurar variables de entorno seguras
- Ejecutar tests de carga

### **PARA CI/CD:**
- Integrar con Newman (Postman CLI)
- Automatizar tests en GitHub Actions
- Generar reportes automáticos

---

## 🎯 **¿Por qué esta colección es profesional?**

✅ **Tokens automáticos** - No necesitas copiar/pegar tokens  
✅ **Validaciones robustas** - Cada test verifica respuestas  
✅ **Casos de error** - Prueba manejo de errores  
✅ **Documentación clara** - Ejemplos y explicaciones  
✅ **Workflow lógico** - Orden recomendado de ejecución  
✅ **Datos realistas** - Ejemplos que funcionan  

**¡Listo para ser usado en entrevistas técnicas o demos profesionales!** 🚀
