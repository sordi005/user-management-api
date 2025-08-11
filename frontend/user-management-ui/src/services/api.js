/**
 * api.js - CLIENTE HTTP PARA USER MANAGEMENT API
 *
 * Configuración de Axios para comunicarse con la API Spring Boot de gestión de usuarios.
 *
 * ENDPOINTS DISPONIBLES EN EL BACKEND:
 * - POST /auth/login          → AuthController.loginUser()
 * - POST /auth/register       → AuthController.registerUser()
 * - GET /users                → UserController.getAllUsers() [ROLE_USER, ROLE_ADMIN]
 * - POST /users               → AdminController.createUser() [ROLE_ADMIN]
 * - PUT /users/{id}           → AdminController.updateUser() [ROLE_ADMIN]
 * - DELETE /users/{id}        → AdminController.deleteUser() [ROLE_ADMIN]
 *
 * CONFIGURACIÓN JWT:
 * - Token almacenado en localStorage como 'auth_token'
 * - Header: Authorization: Bearer {token}
 * - Expiración configurada en application-dev.yml (1 hora por defecto)
 *
 * PROFILES Y URLS:
 * - Desarrollo: http://localhost:8080 (profile dev, docekr-compose)
 * - Producción: https://user-management-api-production-51b2.up.railway.app (profile prod, Railway)
 */

import axios from 'axios';

// ==============================================
// CONFIGURACIÓN DE ENTORNO
// ==============================================

/**
 * URL base según el entorno
 * Desarrollo: Usa el puerto configurado en application-dev.yml (SERVER_PORT=8080)
 * Producción: URL de Railway deployment
 */
const API_BASE_URL = process.env.NODE_ENV === 'production'
  ? 'https://user-management-api-production-51b2.up.railway.app'
  : 'http://localhost:8080';

/**
 * INSTANCIA AXIOS PERSONALIZADA PARA USER MANAGEMENT API
 *
 * Crea un cliente HTTP configurado específicamente para tu Spring Boot application.
 */
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,        //  UserManagementApiApplication (localhost:8080 o Railway)
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',  // Para enviar/recibir JSON a controladores
  },
});

/**
 * REQUEST INTERCEPTOR - AUTENTICACIÓN AUTOMÁTICA
 *
 * - FUNCIÓN PRINCIPAL: Agrega automáticamente el JWT token a TODAS las peticiones
 *
 */
axiosInstance.interceptors.request.use(
  (config) => {
    // Obtener token guardado por tu AuthController.loginUser()
    const token = localStorage.getItem('auth_token');

    // Si hay token, agregarlo al header que espera tu SecurityConfig
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Log para debugging - útil para ver qué endpoints estás llamando
    console.log(`🚀 Llamando a tu API: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ Error preparando petición:', error);
    return Promise.reject(error);
  }
);

/**
 * RESPONSE INTERCEPTOR - MANEJO GLOBAL DE RESPUESTAS Y ERRORES
 **
 * MANEJA AUTOMÁTICAMENTE LOS ERRORES ESPECÍFICOS DE TU SISTEMA:
 **
 * CÓDIGOS DE ERROR QUE DEVUELVE TU BACKEND:
 * - 401 Unauthorized: Token JWT inválido/expirado (JwtTokenProvider validation failed)
 * - 403 Forbidden: Usuario sin permisos (ej: ROLE_USER intentando usar AdminController)
 * - 404 Not Found: Usuario no existe (UserService.findById() no encontró el ID)
 * - 409 Conflict: Datos duplicados (username/email ya existe en PostgreSQL)
 * - 400 Bad Request: Validación falló (@Valid en tus DTOs)
 * - 500 Internal Error: Error en tu código Spring Boot o PostgreSQL
 *
 * ACCIONES AUTOMÁTICAS:
 * - 401: Limpia localStorage y redirige a /login (sesión expirada)
 * - Otros errores: Los propaga para que los manejes en cada componente
 */
axiosInstance.interceptors.response.use(
  (response) => {
    console.log(`✅ Respuesta exitosa de tu API: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message || error.message;

    console.error(`❌ Error desde tu Spring Boot API: ${status} - ${message}`);

    // MANEJO ESPECÍFICO: Token JWT expirado o inválido
    // Tu JwtTokenProvider detectó que el token no es válido
    if (status === 401) {
      localStorage.removeItem('auth_token');
      console.log('🔒 Token expirado - Redirigiendo a login');
      window.location.href = '/login';  // Redirige a tu página de login
    }

    // Propagar error para manejo específico en componentes
    return Promise.reject(error);
  }
);

// ==============================================
// UTILIDADES JWT PARA TU SISTEMA DE AUTENTICACIÓN
// ==============================================

/**
 * Guarda el JWT token devuelto por AuthController.loginUser()
 *
 * RESPUESTA REAL DE LA API:
 * {
 *   "success": true,
 *   "message": "Login exitoso",
 *   "data": {
 *     "access_token": "eyJ...",     ← Token principal
 *     "token_type": "Bearer",
 *     "expires_in": 3600,
 *     "refresh_token": "eyJ...",    ← Para renovar sesión
 *     "issued_at": "2025-08-11T17:34:29.946719421"
 *   },
 *   "status_code": 200,
 *   "time_stamp": "2025-08-11T17:34:29.946Z"
 * }
 */
export const saveToken = (loginResponse) => {
  // Extraer access_token desde la estructura real de tu API
  const accessToken = loginResponse.data.access_token;
  const refreshToken = loginResponse.data.refresh_token;
  const expiresIn = loginResponse.data.expires_in;

  // Guardar tokens en localStorage
  localStorage.setItem('auth_token', accessToken);
  localStorage.setItem('refresh_token', refreshToken);
  localStorage.setItem('token_expires_in', expiresIn);
  localStorage.setItem('token_issued_at', loginResponse.data.issued_at);

  console.log('🔐 JWT Tokens saved:', {
    accessToken: accessToken.substring(0, 20) + '...',
    expiresIn: expiresIn + ' seconds',
    refreshToken: refreshToken ? 'Available' : 'Not provided'
  });
};

/**
 * Obtiene el access_token actual para validaciones o debugging
 */
export const getToken = () => {
  return localStorage.getItem('auth_token');
};

/**
 * Obtiene el refresh_token para renovar la sesión
 */
export const getRefreshToken = () => {
  return localStorage.getItem('refresh_token');
};

/**
 * Elimina todos los tokens (logout completo)
 * Limpia: access_token, refresh_token, expires_in, issued_at
 */
export const removeToken = () => {
  localStorage.removeItem('auth_token');
  localStorage.removeItem('refresh_token');
  localStorage.removeItem('token_expires_in');
  localStorage.removeItem('token_issued_at');
  console.log('🚪 All tokens cleared - Usuario completamente deslogueado');
};

/**
 * Verifica si hay sesión activa y el token no ha expirado
 */
export const isAuthenticated = () => {
  const token = getToken();
  const issuedAt = localStorage.getItem('token_issued_at');
  const expiresIn = localStorage.getItem('token_expires_in');

  if (!token || !issuedAt || !expiresIn) {
    return false;
  }

  // Verificar si el token ha expirado
  const issuedTime = new Date(issuedAt).getTime();
  const expirationTime = issuedTime + (parseInt(expiresIn) * 1000);
  const currentTime = new Date().getTime();

  if (currentTime > expirationTime) {
    console.log('⏰ Token expirado - Limpiando localStorage');
    removeToken();
    return false;
  }

  return true;
};

/**
 * Obtiene información del token para debugging
 */
export const getTokenInfo = () => {
  return {
    hasToken: !!getToken(),
    hasRefreshToken: !!getRefreshToken(),
    expiresIn: localStorage.getItem('token_expires_in'),
    issuedAt: localStorage.getItem('token_issued_at'),
    isAuthenticated: isAuthenticated()
  };
};

// ==============================================
// FUNCIONES DE AUTENTICACIÓN
// ==============================================

/**
 * API de autenticación - Conecta con AuthController
 */
export const authAPI = {
  /**
   * Login de usuario
   * Conecta con: POST /auth/login → AuthController.loginUser()
   *
   * @param {string} username - Username del usuario
   * @param {string} password - Contraseña del usuario
   * @returns {Promise} Respuesta con structure: {success, data: {access_token, refresh_token, ...}, message}
   */
  login: async (username, password) => {
    try {
      const response = await axiosInstance.post('/auth/login', {
        username,
        password
      });
      return response.data; // Tu estructura: {success, data, message, status_code, time_stamp}
    } catch (error) {
      throw error; // El RESPONSE INTERCEPTOR ya maneja errores globalmente
    }
  },

  /**
   * Registro de nuevo usuario
   * Conecta con: POST /auth/register → AuthController.registerUser()
   *
   * @param {Object} userData - Datos del usuario a registrar
   * @param {string} userData.firstName
   * @param {string} userData.lastName
   * @param {string} userData.dateOfBirth
   * @param {string} userData.dni
   * @param {string} userData.email
   * @param {string} userData.username
   * @param {string} userData.password
   * @returns {Promise} Respuesta con usuario creado
   */
  register: async (userData) => {
    try {
      const response = await axiosInstance.post('/auth/register', userData);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

// ==============================================
// FUNCIONES DE GESTIÓN DE USUARIOS
// ==============================================

/**
 * API de usuarios - Conecta con UserController y AdminController
 */
export const userAPI = {
  /**
   * Obtener perfil del usuario autenticado
   * Conecta con: GET /users/me → UserController.getCurrentUser()
   * Requiere: ROLE_USER o ROLE_ADMIN
   *
   * @returns {Promise} Datos del usuario autenticado
   */
  getCurrentUser: async () => {
    try {
      const response = await axiosInstance.get('/users/me');
      return response.data;
    } catch (error) {
      throw error;
    }
  },
  /**
   * Actualizar perfil del usuario autenticado
   * Conecta con: PUT /users/me → UserController.updateCurrentUser()
   * Requiere: ROLE_USER o ROLE_ADMIN
   *
   * @param {Object} userData - Datos actualizados del usuario
   * @returns {Promise} Usuario actualizado
   */
  updateCurrentUser: async (userData) => {
    try {
      const response = await axiosInstance.put('/users/me', userData);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

/**
 * API de administración - Conecta con AdminController REAL
 */
export const adminAPI = {
  /**
   * Obtener lista de usuarios con paginación (Solo Admin)
   * Conecta con: GET /admin/users → AdminController que usa UserService.getAllUsers(page, size)
   * Requiere: ROLE_ADMIN
   *
   * MÉTODO REAL EN TU BACKEND:
   * UserService.getAllUsers(int page, int size) que:
   * - Valida: page >= 0 && size > 0
   * - Ordena por: Sort.by("id")
   * - Retorna: Page<UserResponse> con paginación Spring
   *
   * @param {Object} params - Parámetros de consulta para paginación
   * @param {number} params.page - Número de página (0-based, default: 0)
   * @param {number} params.size - Tamaño de página (debe ser > 0, default: 10)
   * @returns {Promise} Page<UserResponse> con estructura:
   *   {
   *     content: UserResponse[],     // Array de usuarios
   *     pageable: {...},            // Info de paginación
   *     totalElements: number,      // Total de usuarios
   *     totalPages: number,         // Total de páginas
   *     size: number,              // Tamaño de página
   *     number: number,            // Página actual
   *     first: boolean,            // Es primera página
   *     last: boolean              // Es última página
   *   }
   */
  getAllUsers: async (params = { page: 0, size: 10 }) => {
    try {
      // Validaciones del frontend que coinciden con tu UserService
      if (params.page < 0) {
        throw new Error('El número de página debe ser >= 0');
      }
      if (params.size <= 0) {
        throw new Error('El tamaño de página debe ser > 0');
      }

      const response = await axiosInstance.get('/admin/users', { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Crear nuevo usuario (Solo Admin)
   * Conecta con: POST /admin/users/create → AdminController.createUser()
   * Requiere: ROLE_ADMIN
   *
   * @param {Object} userData - Datos del usuario a crear (CreateUserRequest)
   * @param {string} userData.username - Username único
   * @param {string} userData.password - Contraseña
   * @param {string} userData.firstName - Nombre
   * @param {string} userData.lastName - Apellido
   * @param {string} userData.email - Email único
   * @param {string} userData.dni - DNI único
   * @param {string} userData.dateOfBirth - Fecha de nacimiento (YYYY-MM-DD)
   * @param {string} userData.role - Rol (USER/ADMIN)
   * @returns {Promise} ApiResponse<UserResponse> con usuario creado
   */
  createUser: async (userData) => {
    try {
      const response = await axiosInstance.post('/admin/users/create', userData);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

// ==============================================
// FUNCIONES DE UTILIDAD Y HELPERS
// ==============================================

/**
 * Función helper para manejar login completo
 * Realiza login y guarda tokens automáticamente
 *
 * @param {Object} loginData - Datos de login del formulario
 * @param {string} loginData.username - Username
 * @param {string} loginData.password - Password
 * @returns {Promise<Object>} {success: boolean, user?: Object, error?: string}
 */
export const loginUser = async (loginData) => {
  try {
    const response = await authAPI.login(loginData.username, loginData.password);

    if (response.success) {
      // Guardar tokens usando la función saveToken
      saveToken(response);

      return {
        success: true,
        user: response.data,
        message: response.message
      };
    } else {
      return {
        success: false,
        error: response.message || 'Error en login'
      };
    }
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error de conexión'
    };
  }
};

/**
 * Función helper para logout completo
 * Limpia tokens y redirige a login
 */
export const logoutUser = () => {
  removeToken(); // Limpia localStorage
  window.location.href = '/login'; // Redirige a login
};

/**
 * Función helper para registro completo
 * Realiza registro de nuevo usuario
 *
 * @param {Object} userData - Datos del nuevo usuario
 * @returns {Promise<Object>} {success: boolean, data?: Object, error?: string}
 */
export const registerUser = async (userData) => {
  try {
    const response = await authAPI.register(userData);

    if (response.success) {
      return {
        success: true,
        data: response.data,
        message: response.message
      };
    } else {
      return {
        success: false,
        error: response.message || 'Error en registro'
      };
    }
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error de conexión'
    };
  }
};

// Exportar instancia para usar en authService.js y userService.js
export default axiosInstance;
