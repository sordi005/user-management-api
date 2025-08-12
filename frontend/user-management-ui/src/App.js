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
import UserManagement from './components/admin/UserManagement';
import UserProfile from './components/UserProfile';
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

  // Si el usuario está autenticado, mostrar dashboard correspondiente
  if (isLoggedIn) {
    const tokenInfo = getTokenInfo();

    // Verificar si es administrador (decodificar JWT)
    const isAdmin = () => {
      try {
        const token = localStorage.getItem('auth_token');
        if (!token) return false;

        const tokenPayload = JSON.parse(atob(token.split('.')[1]));

        // Verificar diferentes formas en que el rol puede estar en el token
        const role = tokenPayload.role;
        const authorities = tokenPayload.authorities;

        // Verificar si es ADMIN por rol directo o por authorities
        return role === 'ADMIN' || authorities === 'ROLE_ADMIN' ||
               (Array.isArray(authorities) && authorities.includes('ROLE_ADMIN'));
      } catch (error) {
        console.error('Error verificando permisos de admin:', error);
        return false;
      }
    };

    // Si es administrador, mostrar panel de administración
    if (isAdmin()) {
      return (
        <div>
          {/* Header del administrador */}
          <div style={{
            padding: '15px 20px',
            backgroundColor: '#343a40',
            color: 'white',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <h2 style={{ margin: 0 }}>🔧 Panel de Administrador</h2>
            <button
              onClick={handleLogout}
              style={{
                padding: '8px 16px',
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

          {/* Componente de gestión de usuarios */}
          <UserManagement />
        </div>
      );
    }

    // Si es usuario normal, mostrar panel de perfil
    return (
      <div>
        {/* Header del usuario normal */}
        <div style={{
          padding: '15px 20px',
          backgroundColor: '#28a745',
          color: 'white',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <h2 style={{ margin: 0 }}>👤 Panel de Usuario</h2>
          <button
            onClick={handleLogout}
            style={{
              padding: '8px 16px',
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

        {/* Componente de perfil de usuario */}
        <UserProfile />
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
