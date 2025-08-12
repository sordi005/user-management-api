/**
 * api.js - CLIENTE HTTP PARA USER MANAGEMENT API
 *
 * Configuración de Axios para comunicarse con la API Spring Boot de gestión de usuarios.
 *
 * ENDPOINTS DISPONIBLES EN EL BACKEND:
 * - POST /auth/login          → AuthController.loginUser()
 * - POST /auth/register       → AuthController.registerUser()
 * - GET /admin/users          → AdminController.getAllUsers() [ROLE_ADMIN]
 * - POST /admin/users/create  → AdminController.createUser() [ROLE_ADMIN]
 * - PUT /admin/users/{id}     → AdminController.updateUser() [ROLE_ADMIN]
 * - DELETE /admin/users/{id}  → AdminController.deleteUser() [ROLE_ADMIN]
 * - GET /admin/users/{id}     → AdminController.getUserById() [ROLE_ADMIN]
 *
 * CONFIGURACIÓN JWT:
 * - Token almacenado en localStorage como 'auth_token'
 * - Header: Authorization: Bearer {token}
 * - Expiración configurada en application-dev.yml (1 hora por defecto)
 *
 * PROFILES Y URLS:
 * - Desarrollo: http://localhost:8080 (profile dev, docker-compose)
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
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * REQUEST INTERCEPTOR - AUTENTICACIÓN AUTOMÁTICA
 *
 * Agrega automáticamente el JWT token a TODAS las peticiones
 */
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

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
 *
 * CÓDIGOS DE ERROR QUE DEVUELVE TU BACKEND:
 * - 401 Unauthorized: Token JWT inválido/expirado
 * - 403 Forbidden: Usuario sin permisos (ej: ROLE_USER intentando usar AdminController)
 * - 404 Not Found: Usuario no existe
 * - 409 Conflict: Datos duplicados (username/email ya existe)
 * - 400 Bad Request: Validación falló (@Valid en tus DTOs) o BusinessException
 * - 500 Internal Error: Error en tu código Spring Boot o PostgreSQL
 */
axiosInstance.interceptors.response.use(
  (response) => {
    console.log(`✅ Respuesta exitosa de tu API: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const data = error.response?.data;

    // Extraer el mensaje específico del error desde tu ApiResponse
    let message = 'Error desconocido';

    if (data) {
      // Tu backend devuelve ApiResponse con estructura: {success, message, data, status_code, time_stamp, error}
      // El mensaje específico puede estar en 'error' o en 'message' dependiendo del tipo de error
      message = data.error || data.message || error.message;

      console.log('🔍 Datos del error:', data);
      console.log('🔍 Mensaje extraído:', message);
    } else {
      message = error.message;
    }

    console.error(`❌ Error desde tu Spring Boot API: ${status} - ${message}`);

    // Token JWT expirado o inválido - limpiar localStorage y redirigir
    if (status === 401) {
      localStorage.removeItem('auth_token');
      console.log('🔒 Token expirado - Redirigiendo a login');
      window.location.href = '/login';
    }

    // Agregar el mensaje específico al error para que sea accesible en las funciones
    error.specificMessage = message;

    return Promise.reject(error);
  }
);

// ==============================================
// UTILIDADES JWT PARA TU SISTEMA DE AUTENTICACIÓN
// ==============================================

/**
 * Guarda el JWT token devuelto por AuthController.loginUser()
 */
export const saveToken = (loginResponse) => {
  const accessToken = loginResponse.data.access_token;
  const refreshToken = loginResponse.data.refresh_token;
  const expiresIn = loginResponse.data.expires_in;

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

/**
 * Verificar si el usuario actual tiene permisos de administrador
 */
export const isUserAdmin = () => {
  try {
    const token = localStorage.getItem('auth_token');
    if (!token) return false;

    const tokenPayload = JSON.parse(atob(token.split('.')[1]));
    const role = tokenPayload.role;
    const authorities = tokenPayload.authorities;

    return role === 'ADMIN' || authorities === 'ROLE_ADMIN' ||
           (Array.isArray(authorities) && authorities.includes('ROLE_ADMIN'));
  } catch (error) {
    console.error('Error verificando permisos de admin:', error);
    return false;
  }
};

// ==============================================
// API DE AUTENTICACIÓN
// ==============================================

/**
 * API de autenticación - Conecta con AuthController
 */
export const authAPI = {
  /**
   * Login de usuario
   * Conecta con: POST /auth/login → AuthController.loginUser()
   */
  login: async (username, password) => {
    try {
      const response = await axiosInstance.post('/auth/login', {
        username,
        password
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Registro de nuevo usuario
   * Conecta con: POST /auth/register → AuthController.registerUser()
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
// API DE ADMINISTRADOR
// ==============================================
/**
 * API de administración - Conecta con AdminController
 */
export const adminAPI = {
  /**
   * Obtener lista de usuarios con paginación (Solo Admin)
   */
  getAllUsers: async (params = { page: 0, size: 10 }) => {
    try {
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
   */
  createUser: async (userData) => {
    try {
      const response = await axiosInstance.post('/admin/users/create', userData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Obtener usuario por ID (Solo Admin)
   */
  getUserById: async (userId) => {
    try {
      const response = await axiosInstance.get(`/admin/users/${userId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Actualizar usuario por ID (Solo Admin)
   */
  updateUser: async (userId, userData) => {
    try {
      const response = await axiosInstance.put(`/admin/users/${userId}`, userData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Eliminar usuario por ID (Solo Admin)
   */
  deleteUser: async (userId) => {
    try {
      const response = await axiosInstance.delete(`/admin/users/${userId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};
// ==============================================
// API DE USUARIO
// ==============================================
/**
 * API de usuarios - Conecta con UserController
 */
export const userAPI = {
  /**
   * Obtener perfil del usuario autenticado
   * Conecta con: GET /users/me → UserController.getCurrentUser()
   * Requiere: ROLE_USER o ROLE_ADMIN
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

// ==============================================
// FUNCIONES HELPER AUTH
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
 * Función helper para manejar registro completo
 */
export const registerUser = async (userData) => {
  try {
    const response = await authAPI.register(userData);

    return {
      success: true,
      data: response.data,
      message: response.message || 'Usuario registrado exitosamente'
    };
  } catch (error) {
    // ARREGLO: Manejar correctamente los errores HTTP del backend
    console.log('Error en registerUser:', error);
    console.log('Error response:', error.response?.data);
    console.log('Error specificMessage:', error.specificMessage);

    return {
      success: false,
      // Usar el mensaje específico del interceptor
      error: error.specificMessage || 'Error en registro'
    };
  }
};

// ==============================================
// FUNCIONES HELPER PARA ADMINISTRADOR
// ==============================================

/**
 * Helper para obtener todos los usuarios con paginación
 */
export const getAllUsersAdmin = async (page = 0, size = 10) => {
  try {
    const response = await adminAPI.getAllUsers({ page, size });
    return {
      success: true,
      data: response,
      message: 'Usuarios obtenidos exitosamente'
    };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error al obtener usuarios'
    };
  }
};

/**
 * Helper para crear usuario como administrador
 */
export const createUserAdmin = async (userData) => {
  try {
    const response = await adminAPI.createUser(userData);
    return {
      success: true,
      data: response.data,
      message: response.message || 'Usuario creado exitosamente'
    };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error al crear usuario'
    };
  }
};

/**
 * Helper para obtener usuario por ID
 */
export const getUserByIdAdmin = async (userId) => {
  try {
    const response = await adminAPI.getUserById(userId);
    return {
      success: true,
      data: response,
      message: 'Usuario obtenido exitosamente'
    };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error al obtener usuario'
    };
  }
};

/**
 * Helper para actualizar usuario
 */
export const updateUserAdmin = async (userId, userData) => {
  try {
    const response = await adminAPI.updateUser(userId, userData);
    return {
      success: true,
      data: response,
      message: 'Usuario actualizado exitosamente'
    };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error al actualizar usuario'
    };
  }
};

/**
 * Helper para eliminar usuario
 */
export const deleteUserAdmin = async (userId) => {
  try {
    const response = await adminAPI.deleteUser(userId);
    return {
      success: true,
      message: 'Usuario eliminado exitosamente'
    };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error al eliminar usuario'
    };
  }
};

// ==============================================
// FUNCIONES HELPER PARA USUARIOS
// ==============================================

/**
 * Helper para obtener perfil del usuario actual
 */
export const getCurrentUser = async () => {
  try {
    const response = await userAPI.getCurrentUser();
    return {
      success: true,
      data: response,
      message: 'Perfil obtenido exitosamente'
    };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error al obtener perfil'
    };
  }
};

/**
 * Helper para actualizar perfil del usuario actual
 */
export const updateCurrentUser = async (userData) => {
  try {
    const response = await userAPI.updateCurrentUser(userData);
    return {
      success: true,
      data: response,
      message: 'Perfil actualizado exitosamente'
    };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.message || error.message || 'Error al actualizar perfil'
    };
  }
};

// Exportar instancia para usar en otros archivos si es necesario
export default axiosInstance;
