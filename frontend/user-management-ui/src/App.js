/**
 * App.js - APLICACIÓN PRINCIPAL PARA USER MANAGEMENT
 *
 * Componente raíz que maneja la navegación básica entre:
 * - LoginForm: Para autenticación de usuarios existentes
 * - RegisterForm: Para registro de nuevos usuarios
 * - UserManagement: Dashboard admin (próximamente)
 *
 * NAVEGACIÓN SIMPLE SIN REACT ROUTER (por ahora):
 * - Estado local para cambiar entre vistas
 * - Botones para alternar entre login/registro
 */

import React, { useState } from 'react';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm';
import { isAuthenticated, getTokenInfo } from './services/api';

function App() {
  // ===========================
  // ESTADO DE LA APLICACIÓN
  // ===========================

  // Vista actual: 'login' | 'register' | 'dashboard'
  const [currentView, setCurrentView] = useState('login');

  // Estado de autenticación
  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());

  // ===========================
  // FUNCIONES DE NAVEGACIÓN
  // ===========================

  /**
   * Cambia a la vista de login
   */
  const showLogin = () => {
    setCurrentView('login');
  };

  /**
   * Cambia a la vista de registro
   */
  const showRegister = () => {
    setCurrentView('register');
  };

  /**
   * NUEVO: Maneja el login exitoso
   * Se ejecuta cuando LoginForm completa la autenticación
   */
  const handleLoginSuccess = () => {
    setIsLoggedIn(true);
    console.log('🎉 Estado de autenticación actualizado - Redirigiendo al dashboard');
  };

  /**
   * Maneja logout del usuario
   */
  const handleLogout = () => {
    localStorage.clear();
    setIsLoggedIn(false);
    setCurrentView('login');
    console.log('Usuario deslogueado');
  };

  // ===========================
  // RENDERIZADO CONDICIONAL
  // ===========================

  // Si el usuario está autenticado, mostrar dashboard básico
  if (isLoggedIn) {
    const tokenInfo = getTokenInfo();

    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>
        <h1>🎉 ¡Bienvenido al User Management System!</h1>
        <p>✅ Estás autenticado correctamente</p>

        {/* Info de debugging */}
        <div style={{
          margin: '20px auto',
          padding: '15px',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px',
          maxWidth: '500px',
          textAlign: 'left'
        }}>
          <h3>📊 Estado de tu sesión:</h3>
          <ul>
            <li>Token activo: {tokenInfo.hasToken ? '✅ Sí' : '❌ No'}</li>
            <li>Refresh token: {tokenInfo.hasRefreshToken ? '✅ Disponible' : '❌ No disponible'}</li>
            <li>Expira en: {tokenInfo.expiresIn} segundos</li>
            <li>Emitido: {tokenInfo.issuedAt}</li>
          </ul>
        </div>

        {/* Acciones disponibles */}
        <div style={{ marginTop: '30px' }}>
          <h3>🔧 Próximas funcionalidades:</h3>
          <p>• UserManagement (CRUD de usuarios)</p>
          <p>• Panel de administrador</p>
          <p>• Perfil de usuario</p>
        </div>

        {/* Botón de logout */}
        <button
          onClick={handleLogout}
          style={{
            marginTop: '30px',
            padding: '10px 20px',
            backgroundColor: '#dc3545',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          🚪 Cerrar Sesión
        </button>
      </div>
    );
  }

  // Si no está autenticado, mostrar login o registro
  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      {/* Header de navegación */}
      <div style={{
        padding: '20px',
        textAlign: 'center',
        backgroundColor: 'white',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
      }}>
        <h1>User Management API - Frontend</h1>
        <p>Conectando con tu Spring Boot API en Railway</p>

        {/* Botones de navegación */}
        <div style={{ marginTop: '15px' }}>
          <button
            onClick={showLogin}
            style={{
              padding: '8px 16px',
              marginRight: '10px',
              backgroundColor: currentView === 'login' ? '#007bff' : '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Iniciar Sesión
          </button>

          <button
            onClick={showRegister}
            style={{
              padding: '8px 16px',
              backgroundColor: currentView === 'register' ? '#28a745' : '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Crear Cuenta
          </button>
        </div>
      </div>

      {/* Contenido principal */}
      <div style={{ padding: '20px' }}>
        {currentView === 'login' && <LoginForm onLoginSuccess={handleLoginSuccess} />}
        {currentView === 'register' && <RegisterForm />}
      </div>

      {/* Footer informativo */}
      <div style={{
        marginTop: '50px',
        padding: '20px',
        textAlign: 'center',
        backgroundColor: 'white',
        color: '#6c757d'
      }}>
        <p>🚀 <strong>Backend:</strong> https://user-management-api-production-51b2.up.railway.app</p>
        <p>🔧 <strong>Tecnologías:</strong> React + Spring Boot + PostgreSQL + JWT</p>
      </div>
    </div>
  );
}

export default App;
