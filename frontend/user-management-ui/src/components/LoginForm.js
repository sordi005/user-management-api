/**
 * LoginForm.js - FORMULARIO DE AUTENTICACIÓN
 *
 * Componente básico que se conecta con AuthController.loginUser()
 * Endpoint: POST /auth/login
 *
 * FUNCIONALIDAD:
 * - Captura username/password del usuario
 * - Envía LoginRequest a  Spring Boot API
 * - Almacena JWT token en localStorage
 * - Redirige al dashboard en caso de éxito
 */

import React, { useState } from 'react';
import { loginUser } from '../services/api';

const LoginForm = () => {
  // ===========================
  // ESTADO DEL COMPONENTE
  // ===========================

  // Datos del formulario (corresponden a LoginRequest.java)
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });

  // Estados de UI
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // ===========================
  // MANEJADORES DE EVENTOS
  // ===========================

  /**
   * Actualiza el estado cuando el usuario escribe en los inputs
   */
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  /**
   * Envía el formulario a AuthController.loginUser()
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Limpiar mensajes anteriores
    setError('');
    setMessage('');
    setLoading(true);

    try {
      // Llamar a la API (esto usa tu AuthController)
      const response = await loginUser(formData);

      // Si el login es exitoso, tu API devuelve:
      // { success: true, data: { access_token: "...", ... } }
      if (response.data.success) {
        setMessage('¡Login exitoso! Redirigiendo...');

        // El token se guarda automáticamente en api.js
        // TODO: Aquí redirigirías al dashboard
        console.log('Usuario logueado:', response.data);
      }

    } catch (error) {
      // Manejar errores del backend (401, 400, etc.)
      const errorMessage = error.response?.data?.message || 'Error de conexión';
      setError(errorMessage);
      console.error('Error en login:', error);
    } finally {
      setLoading(false);
    }
  };

  // ===========================
  // RENDERIZADO DEL COMPONENTE
  // ===========================

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px' }}>
      <h2>Iniciar Sesión</h2>
      <p>Conectando con User Management API</p>

      <form onSubmit={handleSubmit}>
        {/* Campo Username */}
        <div style={{ marginBottom: '15px' }}>
          <label>Usuario:</label>
          <input
            type="text"
            name="username"
            value={formData.username}
            onChange={handleInputChange}
            required
            style={{
              width: '100%',
              padding: '8px',
              marginTop: '5px',
              border: '1px solid #ccc',
              borderRadius: '4px'
            }}
          />
        </div>

        {/* Campo Password */}
        <div style={{ marginBottom: '15px' }}>
          <label>Contraseña:</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            required
            style={{
              width: '100%',
              padding: '8px',
              marginTop: '5px',
              border: '1px solid #ccc',
              borderRadius: '4px'
            }}
          />
        </div>

        {/* Botón Submit */}
        <button
          type="submit"
          disabled={loading}
          style={{
            width: '100%',
            padding: '10px',
            backgroundColor: loading ? '#ccc' : '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: loading ? 'not-allowed' : 'pointer'
          }}
        >
          {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
        </button>
      </form>

      {/* Mensajes de estado */}
      {message && (
        <div style={{ marginTop: '15px', padding: '10px', backgroundColor: '#d4edda', color: '#155724', borderRadius: '4px' }}>
          {message}
        </div>
      )}

      {error && (
        <div style={{ marginTop: '15px', padding: '10px', backgroundColor: '#f8d7da', color: '#721c24', borderRadius: '4px' }}>
          {error}
        </div>
      )}
    </div>
  );
};

export default LoginForm;
