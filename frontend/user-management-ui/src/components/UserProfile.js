/**
 * UserProfile.js - PANEL DE PERFIL DE USUARIO
 *
 * FUNCIONALIDADES IMPLEMENTADAS:
 * ✅ Ver perfil personal (GET /users/me)
 * ✅ Editar perfil personal (PUT /users/me)
 * ✅ Cambiar datos personales (nombre, apellido, email, DNI, fecha nacimiento)
 * ✅ Validación de formularios
 * ✅ Mensajes de éxito y error
 *
 * CONECTA CON TU BACKEND:
 * - UserController.getCurrentUser() → Ver perfil
 * - UserController.updateCurrentUser() → Actualizar perfil
 *
 * REQUIERE: ROLE_USER o ROLE_ADMIN (verificado por @PreAuthorize en backend)
 */

import React, { useState, useEffect } from 'react';
import { getCurrentUser, updateCurrentUser } from '../services/api';

const UserProfile = () => {
  // ===========================
  // ESTADO DEL COMPONENTE
  // ===========================

  // Datos del usuario
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modo de edición
  const [isEditing, setIsEditing] = useState(false);

  // Formulario para editar datos
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    dni: '',
    dateOfBirth: ''
  });

  // ===========================
  // EFECTOS Y CARGA INICIAL
  // ===========================

  useEffect(() => {
    loadUserProfile();
  }, []);

  // ===========================
  // FUNCIONES DE CARGA DE DATOS
  // ===========================

  /**
   * Cargar perfil del usuario desde UserController.getCurrentUser()
   */
  const loadUserProfile = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await getCurrentUser();

      if (response.success) {
        setUser(response.data);
        // Pre-llenar formulario con datos actuales
        setFormData({
          firstName: response.data.firstName || '',
          lastName: response.data.lastName || '',
          email: response.data.email || '',
          dni: response.data.dni || '',
          dateOfBirth: response.data.dateOfBirth || ''
        });
      } else {
        setError(response.error || 'Error al cargar perfil');
      }
    } catch (err) {
      setError('Error de conexión al cargar perfil');
      console.error('Error loading profile:', err);
    } finally {
      setLoading(false);
    }
  };

  // ===========================
  // FUNCIONES DE FORMULARIO
  // ===========================

  /**
   * Actualizar perfil del usuario - conecta con UserController.updateCurrentUser()
   */
  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await updateCurrentUser(formData);

      if (response.success) {
        setSuccess('Perfil actualizado exitosamente');
        setUser(response.data);
        setIsEditing(false);

        // Actualizar formData con los nuevos datos por si hay cambios del backend
        setFormData({
          firstName: response.data.firstName || '',
          lastName: response.data.lastName || '',
          email: response.data.email || '',
          dni: response.data.dni || '',
          dateOfBirth: response.data.dateOfBirth || ''
        });
      } else {
        setError(response.error || 'Error al actualizar perfil');
      }
    } catch (err) {
      setError('Error de conexión al actualizar perfil');
      console.error('Error updating profile:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleCancelEdit = () => {
    // Restaurar datos originales
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        dni: user.dni || '',
        dateOfBirth: user.dateOfBirth || ''
      });
    }
    setIsEditing(false);
    setError('');
    setSuccess('');
  };

  // ===========================
  // RENDER DEL COMPONENTE
  // ===========================

  if (loading && !user) {
    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>
        <div style={{ fontSize: '18px', color: '#666' }}>🔄 Cargando perfil...</div>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      {/* HEADER */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '30px',
        borderBottom: '2px solid #e9ecef',
        paddingBottom: '15px'
      }}>
        <h1 style={{ margin: 0, color: '#333' }}>👤 Mi Perfil</h1>
        {!isEditing && (
          <button
            onClick={() => setIsEditing(true)}
            style={{
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              padding: '12px 24px',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            ✏️ Editar Perfil
          </button>
        )}
      </div>

      {/* MENSAJES DE ESTADO */}
      {error && (
        <div style={{
          backgroundColor: '#f8d7da',
          color: '#721c24',
          padding: '12px',
          borderRadius: '6px',
          marginBottom: '20px',
          border: '1px solid #f5c6cb'
        }}>
          ❌ {error}
        </div>
      )}

      {success && (
        <div style={{
          backgroundColor: '#d4edda',
          color: '#155724',
          padding: '12px',
          borderRadius: '6px',
          marginBottom: '20px',
          border: '1px solid #c3e6cb'
        }}>
          ✅ {success}
        </div>
      )}

      {/* CONTENIDO PRINCIPAL */}
      {user && (
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          padding: '30px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          border: '1px solid #dee2e6'
        }}>

          {/* MODO VER PERFIL */}
          {!isEditing ? (
            <div>
              <h2 style={{ marginBottom: '25px', color: '#333' }}>📊 Información Personal</h2>

              <div style={{ display: 'grid', gap: '20px' }}>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '15px',
                  padding: '15px',
                  backgroundColor: '#f8f9fa',
                  borderRadius: '6px'
                }}>
                  <strong>ID de Usuario:</strong>
                  <span>{user.id}</span>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '15px',
                  padding: '15px'
                }}>
                  <strong>Username:</strong>
                  <span style={{ fontFamily: 'monospace', fontWeight: 'bold' }}>{user.username}</span>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '15px',
                  padding: '15px',
                  backgroundColor: '#f8f9fa',
                  borderRadius: '6px'
                }}>
                  <strong>Nombre completo:</strong>
                  <span>{user.firstName} {user.lastName}</span>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '15px',
                  padding: '15px'
                }}>
                  <strong>Email:</strong>
                  <span>{user.email}</span>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '15px',
                  padding: '15px',
                  backgroundColor: '#f8f9fa',
                  borderRadius: '6px'
                }}>
                  <strong>DNI:</strong>
                  <span>{user.dni}</span>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '15px',
                  padding: '15px'
                }}>
                  <strong>Fecha de Nacimiento:</strong>
                  <span>{user.dateOfBirth}</span>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '15px',
                  padding: '15px',
                  backgroundColor: '#f8f9fa',
                  borderRadius: '6px'
                }}>
                  <strong>Rol:</strong>
                  <span style={{
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '12px',
                    fontWeight: 'bold',
                    backgroundColor: user.role === 'ADMIN' ? '#dc3545' : '#28a745',
                    color: 'white',
                    display: 'inline-block'
                  }}>
                    {user.role}
                  </span>
                </div>

                {user.createdAt && (
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: '150px 1fr',
                    gap: '15px',
                    padding: '15px'
                  }}>
                    <strong>Miembro desde:</strong>
                    <span>{new Date(user.createdAt).toLocaleDateString()}</span>
                  </div>
                )}
              </div>
            </div>
          ) : (
            /* MODO EDITAR PERFIL */
            <div>
              <h2 style={{ marginBottom: '25px', color: '#333' }}>✏️ Editar Información Personal</h2>

              <form onSubmit={handleUpdateProfile}>
                <div style={{ display: 'grid', gap: '20px' }}>

                  {/* Información no editable */}
                  <div style={{
                    padding: '15px',
                    backgroundColor: '#e9ecef',
                    borderRadius: '6px',
                    border: '1px solid #ced4da'
                  }}>
                    <small style={{ color: '#6c757d', fontWeight: 'bold' }}>
                      ℹ️ <strong>Información fija:</strong> Username: {user.username} | ID: {user.id} | Rol: {user.role}
                    </small>
                  </div>

                  {/* Campos editables */}
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                    <div>
                      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                        Nombre:
                      </label>
                      <input
                        type="text"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        required
                        style={{
                          width: '100%',
                          padding: '10px',
                          borderRadius: '4px',
                          border: '1px solid #ddd',
                          fontSize: '14px'
                        }}
                      />
                    </div>

                    <div>
                      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                        Apellido:
                      </label>
                      <input
                        type="text"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        required
                        style={{
                          width: '100%',
                          padding: '10px',
                          borderRadius: '4px',
                          border: '1px solid #ddd',
                          fontSize: '14px'
                        }}
                      />
                    </div>
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                      Email:
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      required
                      style={{
                        width: '100%',
                        padding: '10px',
                        borderRadius: '4px',
                        border: '1px solid #ddd',
                        fontSize: '14px'
                      }}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                    <div>
                      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                        DNI:
                      </label>
                      <input
                        type="text"
                        name="dni"
                        value={formData.dni}
                        onChange={handleInputChange}
                        required
                        style={{
                          width: '100%',
                          padding: '10px',
                          borderRadius: '4px',
                          border: '1px solid #ddd',
                          fontSize: '14px'
                        }}
                      />
                    </div>

                    <div>
                      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                        Fecha de Nacimiento:
                      </label>
                      <input
                        type="date"
                        name="dateOfBirth"
                        value={formData.dateOfBirth}
                        onChange={handleInputChange}
                        required
                        style={{
                          width: '100%',
                          padding: '10px',
                          borderRadius: '4px',
                          border: '1px solid #ddd',
                          fontSize: '14px'
                        }}
                      />
                    </div>
                  </div>

                  {/* Nota informativa */}
                  <div style={{
                    padding: '15px',
                    backgroundColor: '#fff3cd',
                    borderRadius: '6px',
                    border: '1px solid #ffeaa7'
                  }}>
                    <small style={{ color: '#856404' }}>
                      💡 <strong>Nota:</strong> El username y rol no se pueden modificar. Para cambios de contraseña, contacta al administrador.
                    </small>
                  </div>
                </div>

                {/* Botones */}
                <div style={{
                  display: 'flex',
                  justifyContent: 'flex-end',
                  gap: '15px',
                  marginTop: '30px'
                }}>
                  <button
                    type="button"
                    onClick={handleCancelEdit}
                    style={{
                      padding: '12px 24px',
                      borderRadius: '6px',
                      border: '1px solid #ddd',
                      backgroundColor: '#f8f9fa',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    ❌ Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    style={{
                      padding: '12px 24px',
                      borderRadius: '6px',
                      border: 'none',
                      backgroundColor: loading ? '#ccc' : '#28a745',
                      color: 'white',
                      cursor: loading ? 'not-allowed' : 'pointer',
                      fontSize: '14px',
                      fontWeight: 'bold'
                    }}
                  >
                    {loading ? '🔄 Guardando...' : '💾 Guardar Cambios'}
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default UserProfile;
