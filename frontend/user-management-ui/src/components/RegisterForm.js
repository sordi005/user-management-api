/**
 * RegisterForm.js - FORMULARIO DE REGISTRO
 *
 * Componente que se conecta con AuthController.registerUser()
 * Endpoint: POST /auth/register
 *
 * FUNCIONALIDAD:
 * - Captura datos de registro del nuevo usuario
 * - Envía CreateUserRequest a tu Spring Boot API
 * - Valida datos básicos del formulario
 * - Redirige al login tras registro exitoso
 */

import React, { useState } from 'react';
import { registerUser } from '../services/api';

const RegisterForm = () => {
  // ===========================
  // ESTADO DEL COMPONENTE
  // ===========================

  // Datos del formulario (corresponden a CreateUserRequest.java)
  const [formData, setFormData] = useState({
      firstName: '',
      lastName: '',
      dni: '',
      email: '',
      username: '',
      password: '',
      confirmPassword: ''
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
   * Validaciones básicas del formulario
   */
  const validateForm = () => {
    // Verificar que las contraseñas coincidan
    if (formData.password !== formData.confirmPassword) {
      setError('Las contraseñas no coinciden');
      return false;
    }

    // Validar longitud mínima de contraseña
    if (formData.password.length < 8) {
      setError('La contraseña debe tener al menos 6 caracteres');
      return false;
    }

    // Validar email básico
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('Por favor ingresa un email válido');
      return false;
    }

    return true;
  };

  /**
   * Envía el formulario a AuthController.registerUser()
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Limpiar mensajes anteriores
    setError('');
    setMessage('');

    // Validar formulario
    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      // Preparar datos para enviar (sin confirmPassword)
      const registerData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        dni: formData.dni,
        email: formData.email,
        username: formData.username,
        password: formData.password
      };

      // Llamar a la API (esto usa tu AuthController)
      const response = await registerUser(registerData);

      // Si el registro es exitoso
      if (response.success) {
        setMessage('¡Registro exitoso! Ya puedes iniciar sesión.');

        // Limpiar formulario
        setFormData({
            firstName: '',
            lastName: '',
            dni : "",
            email: '',
            username: '',
            password: '',
            confirmPassword: ''
        });

        // TODO: Redirigir al login después de unos segundos
        console.log('Usuario registrado:', response.data);
      }

    } catch (error) {
      // Manejar errores del backend (409 Conflict, 400 Bad Request, etc.)
      const errorMessage = error.response?.data?.message || error.message || 'Error de conexión';
      setError(errorMessage);
      console.error('Error en registro:', error);
    } finally {
      setLoading(false);
    }
  };

  // ===========================
  // RENDERIZADO DEL COMPONENTE
  // ===========================

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px' }}>
      <h2>Crear Cuenta</h2>
      <p>Registro en User Management API</p>

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

        {/* Campo Email */}
        <div style={{ marginBottom: '15px' }}>
          <label>Email:</label>
          <input
            type="email"
            name="email"
            value={formData.email}
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

        {/* Campo First Name */}
        <div style={{ marginBottom: '15px' }}>
          <label>Nombre:</label>
          <input
            type="text"
            name="firstName"
            value={formData.firstName}
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

        {/* Campo Last Name */}
        <div style={{ marginBottom: '15px' }}>
          <label>Apellido:</label>
          <input
            type="text"
            name="lastName"
            value={formData.lastName}
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

        {/* Campo Confirm Password */}
        <div style={{ marginBottom: '15px' }}>
          <label>Confirmar Contraseña:</label>
          <input
            type="password"
            name="confirmPassword"
            value={formData.confirmPassword}
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
            backgroundColor: loading ? '#ccc' : '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: loading ? 'not-allowed' : 'pointer'
          }}
        >
          {loading ? 'Registrando...' : 'Crear Cuenta'}
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

      {/* Link para ir al login */}
      <div style={{ marginTop: '20px', textAlign: 'center' }}>
        <p>¿Ya tienes cuenta?
          <button
            onClick={() => {/* TODO: Navegar a login */}}
            style={{
              background: 'none',
              border: 'none',
              color: '#007bff',
              textDecoration: 'underline',
              cursor: 'pointer',
              marginLeft: '5px'
            }}
          >
            Iniciar Sesión
          </button>
        </p>
      </div>
    </div>
  );
};

export default RegisterForm;
