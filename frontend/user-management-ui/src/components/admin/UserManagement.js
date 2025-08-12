/**
 * UserManagement.js - PANEL DE ADMINISTRACIÓN
 *
 * REQUIERE: ROLE_ADMIN (verificado por @PreAuthorize en backend)
 */

import React, { useState, useEffect } from 'react';
import {
  getAllUsersAdmin,
  createUserAdmin,
  updateUserAdmin,
  deleteUserAdmin,
  getUserByIdAdmin
} from '../../services/api';

const UserManagement = () => {
  // ===========================
  // ESTADO DEL COMPONENTE
  // ===========================

  // Lista de usuarios y paginación
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Paginación (coincide con tu UserService.getAllUsers)
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Modales y formularios
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  // Formulario para crear/editar usuario
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    dni: '',
    dateOfBirth: '',
    email: '',
    username: '',
    password: '',
    role: 'USER' // Default role
  });

  // ===========================
  // EFECTOS Y CARGA INICIAL
  // ===========================

  useEffect(() => {
    loadUsers();
  }, [currentPage, pageSize]);

  // ===========================
  // FUNCIONES DE CARGA DE DATOS
  // ===========================

  /**
   * Cargar lista de usuarios desde AdminController.getAllUsers()
   */
  const loadUsers = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await getAllUsersAdmin(currentPage, pageSize);

      if (response.success) {
        const pageData = response.data;
        setUsers(pageData.content || []);
        setTotalPages(pageData.totalPages || 0);
        setTotalElements(pageData.totalElements || 0);
      } else {
        setError(response.error || 'Error al cargar usuarios');
      }
    } catch (err) {
      setError('Error de conexión al cargar usuarios');
      console.error('Error loading users:', err);
    } finally {
      setLoading(false);
    }
  };

  // ===========================
  // FUNCIONES CRUD
  // ===========================

  /**
   * Crear nuevo usuario - conecta con AdminController.createUser()
   */
  const handleCreateUser = async (e) => {
    e.preventDefault(); // Prevenir el envío del formulario
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await createUserAdmin(formData);

      if (response.success) {
        setSuccess('Usuario creado exitosamente');
        setShowCreateModal(false);
        resetForm();
        loadUsers(); // Recargar lista
      } else {
        setError(response.error || 'Error al crear usuario');
      }
    } catch (err) {
      setError('Error de conexión al crear usuario');
      console.error('Error creating user:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Actualizar usuario existente - conecta con AdminController.updateUser()
   */
  const handleUpdateUser = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await updateUserAdmin(selectedUser.id, formData);

      if (response.success) {
        setSuccess('Usuario actualizado exitosamente');
        setShowEditModal(false);
        resetForm();
        loadUsers(); // Recargar lista
      } else {
        setError(response.error || 'Error al actualizar usuario');
      }
    } catch (err) {
      setError('Error de conexión al actualizar usuario');
      console.error('Error updating user:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Eliminar usuario - conecta con AdminController.deleteUser()
   */
  const handleDeleteUser = async (userId) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await deleteUserAdmin(userId);

      if (response.success) {
        setSuccess('Usuario eliminado exitosamente');
        loadUsers(); // Recargar lista
      } else {
        setError(response.error || 'Error al eliminar usuario');
      }
    } catch (err) {
      setError('Error de conexión al eliminar usuario');
      console.error('Error deleting user:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Ver detalles de usuario - conecta con AdminController.getUserById()
   */
  const handleViewUser = async (userId) => {
    setLoading(true);
    setError('');

    try {
      const response = await getUserByIdAdmin(userId);

      if (response.success) {
        setSelectedUser(response.data);
        setShowDetailsModal(true);
      } else {
        setError(response.error || 'Error al obtener detalles del usuario');
      }
    } catch (err) {
      setError('Error de conexión al obtener usuario');
      console.error('Error fetching user:', err);
    } finally {
      setLoading(false);
    }
  };

  // ===========================
  // FUNCIONES DE FORMULARIO
  // ===========================

  const resetForm = () => {
    setFormData({
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      email: '',
      dni: '',
      dateOfBirth: '',
      role: 'USER'
    });
    setSelectedUser(null);
  };

  const openCreateModal = () => {
    resetForm();
    setShowCreateModal(true);
  };

  const openEditModal = (user) => {
    setSelectedUser(user);
    setFormData({
      username: user.username || '',
      password: '', // Siempre vacío por seguridad
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      email: user.email || '',
      dni: user.dni || '',
      dateOfBirth: user.dateOfBirth || '',
      role: user.role || 'USER'
    });
    setShowEditModal(true);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // ===========================
  // FUNCIONES DE PAGINACIÓN
  // ===========================

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  const handlePageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setCurrentPage(0); // Reset a primera página
  };

  // ===========================
  // RENDER DEL COMPONENTE
  // ===========================

  return (
    <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
      {/* HEADER */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '30px',
        borderBottom: '2px solid #e9ecef',
        paddingBottom: '15px'
      }}>
        <h1 style={{ margin: 0, color: '#333' }}>👥 Gestión de Usuarios</h1>
        <button
          onClick={openCreateModal}
          style={{
            backgroundColor: '#28a745',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '6px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: 'bold'
          }}
        >
          ➕ Crear Usuario
        </button>
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

      {/* CONTROLES DE PAGINACIÓN SUPERIOR */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '20px',
        padding: '15px',
        backgroundColor: '#f8f9fa',
        borderRadius: '6px'
      }}>
        <div>
          <strong>Total usuarios: {totalElements}</strong>
          <span style={{ marginLeft: '20px', color: '#666' }}>
            Página {currentPage + 1} de {totalPages}
          </span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <label>Usuarios por página:</label>
          <select
            value={pageSize}
            onChange={handlePageSizeChange}
            style={{ padding: '5px', borderRadius: '4px', border: '1px solid #ddd' }}
          >
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={20}>20</option>
            <option value={50}>50</option>
          </select>
        </div>
      </div>

      {/* TABLA DE USUARIOS */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <div style={{ fontSize: '18px', color: '#666' }}>🔄 Cargando usuarios...</div>
        </div>
      ) : (
        <div style={{
          border: '1px solid #dee2e6',
          borderRadius: '8px',
          overflow: 'hidden',
          backgroundColor: 'white',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: '#343a40', color: 'white' }}>
                <th style={{ padding: '15px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>ID</th>
                <th style={{ padding: '15px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>Usuario</th>
                <th style={{ padding: '15px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>Nombre</th>
                <th style={{ padding: '15px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>Email</th>
                <th style={{ padding: '15px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>DNI</th>
                <th style={{ padding: '15px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>Rol</th>
                <th style={{ padding: '15px', textAlign: 'center', borderBottom: '2px solid #dee2e6' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{
                    padding: '40px',
                    textAlign: 'center',
                    color: '#666',
                    backgroundColor: '#f8f9fa'
                  }}>
                    📭 No hay usuarios registrados
                  </td>
                </tr>
              ) : (
                users.map((user, index) => (
                  <tr
                    key={user.id}
                    style={{
                      backgroundColor: index % 2 === 0 ? 'white' : '#f8f9fa',
                      borderBottom: '1px solid #dee2e6'
                    }}
                  >
                    <td style={{ padding: '12px' }}>{user.id}</td>
                    <td style={{ padding: '12px', fontWeight: 'bold' }}>{user.username}</td>
                    <td style={{ padding: '12px' }}>{user.firstName} {user.lastName}</td>
                    <td style={{ padding: '12px' }}>{user.email}</td>
                    <td style={{ padding: '12px' }}>{user.dni}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        backgroundColor: user.role === 'ADMIN' ? '#dc3545' : '#007bff',
                        color: 'white'
                      }}>
                        {user.role}
                      </span>
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center' }}>
                      <div style={{ display: 'flex', gap: '8px', justifyContent: 'center' }}>
                        <button
                          onClick={() => handleViewUser(user.id)}
                          style={{
                            backgroundColor: '#17a2b8',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                          title="Ver detalles"
                        >
                          👁️ Ver
                        </button>
                        <button
                          onClick={() => openEditModal(user)}
                          style={{
                            backgroundColor: '#ffc107',
                            color: '#212529',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                          title="Editar usuario"
                        >
                          ✏️ Editar
                        </button>
                        <button
                          onClick={() => handleDeleteUser(user.id)}
                          style={{
                            backgroundColor: '#dc3545',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                          title="Eliminar usuario"
                        >
                          🗑️ Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* CONTROLES DE PAGINACIÓN INFERIOR */}
      {totalPages > 1 && (
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          marginTop: '20px',
          gap: '10px'
        }}>
          <button
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 0}
            style={{
              padding: '8px 16px',
              borderRadius: '4px',
              border: '1px solid #ddd',
              backgroundColor: currentPage === 0 ? '#f8f9fa' : 'white',
              cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
              color: currentPage === 0 ? '#999' : '#333'
            }}
          >
            ← Anterior
          </button>

          <span style={{
            padding: '8px 16px',
            backgroundColor: '#007bff',
            color: 'white',
            borderRadius: '4px',
            fontWeight: 'bold'
          }}>
            {currentPage + 1} / {totalPages}
          </span>

          <button
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
            style={{
              padding: '8px 16px',
              borderRadius: '4px',
              border: '1px solid #ddd',
              backgroundColor: currentPage >= totalPages - 1 ? '#f8f9fa' : 'white',
              cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer',
              color: currentPage >= totalPages - 1 ? '#999' : '#333'
            }}
          >
            Siguiente →
          </button>
        </div>
      )}

      {/* MODAL CREAR USUARIO */}
      {showCreateModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '8px',
            width: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ marginBottom: '20px', color: '#333' }}>➕ Crear Nuevo Usuario</h2>

            <form onSubmit={handleCreateUser}>
              <div style={{ display: 'grid', gap: '15px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                    Username:
                  </label>
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
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
                    Contraseña:
                  </label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
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

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
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

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
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

                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                    Rol:
                  </label>
                  <select
                    name="role"
                    value={formData.role}
                    onChange={handleInputChange}
                    required
                    style={{
                      width: '100%',
                      padding: '10px',
                      borderRadius: '4px',
                      border: '1px solid #ddd',
                      fontSize: '14px'
                    }}
                  >
                    <option value="USER">Usuario</option>
                    <option value="ADMIN">Administrador</option>
                  </select>
                </div>
              </div>

              <div style={{
                display: 'flex',
                justifyContent: 'flex-end',
                gap: '10px',
                marginTop: '25px'
              }}>
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '4px',
                    border: '1px solid #ddd',
                    backgroundColor: '#f8f9fa',
                    cursor: 'pointer'
                  }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '4px',
                    border: 'none',
                    backgroundColor: '#28a745',
                    color: 'white',
                    cursor: loading ? 'not-allowed' : 'pointer'
                  }}
                >
                  {loading ? 'Creando...' : 'Crear Usuario'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL EDITAR USUARIO */}
      {showEditModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '8px',
            width: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ marginBottom: '20px', color: '#333' }}>
              ✏️ Editar Usuario: {selectedUser?.username}
            </h2>

            <form onSubmit={handleUpdateUser}>
              <div style={{ display: 'grid', gap: '15px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                    Username:
                  </label>
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
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

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
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

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
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

                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                    Rol:
                  </label>
                  <select
                    name="role"
                    value={formData.role}
                    onChange={handleInputChange}
                    required
                    style={{
                      width: '100%',
                      padding: '10px',
                      borderRadius: '4px',
                      border: '1px solid #ddd',
                      fontSize: '14px'
                    }}
                  >
                    <option value="USER">Usuario</option>
                    <option value="ADMIN">Administrador</option>
                  </select>
                </div>

                <div style={{
                  padding: '10px',
                  backgroundColor: '#fff3cd',
                  borderRadius: '4px',
                  border: '1px solid #ffeaa7'
                }}>
                  <small style={{ color: '#856404' }}>
                    💡 <strong>Nota:</strong> Deja la contraseña vacía para mantener la actual
                  </small>
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                    Nueva Contraseña (opcional):
                  </label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    placeholder="Dejar vacío para mantener la actual"
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

              <div style={{
                display: 'flex',
                justifyContent: 'flex-end',
                gap: '10px',
                marginTop: '25px'
              }}>
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '4px',
                    border: '1px solid #ddd',
                    backgroundColor: '#f8f9fa',
                    cursor: 'pointer'
                  }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '4px',
                    border: 'none',
                    backgroundColor: '#ffc107',
                    color: '#212529',
                    cursor: loading ? 'not-allowed' : 'pointer'
                  }}
                >
                  {loading ? 'Actualizando...' : 'Actualizar Usuario'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL VER DETALLES */}
      {showDetailsModal && selectedUser && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '8px',
            width: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ marginBottom: '20px', color: '#333' }}>
              👁️ Detalles del Usuario
            </h2>

            <div style={{ display: 'grid', gap: '15px' }}>
              <div style={{
                display: 'grid',
                gridTemplateColumns: '150px 1fr',
                gap: '10px',
                padding: '10px',
                backgroundColor: '#f8f9fa',
                borderRadius: '4px'
              }}>
                <strong>ID:</strong>
                <span>{selectedUser.id}</span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '150px 1fr',
                gap: '10px',
                padding: '10px'
              }}>
                <strong>Username:</strong>
                <span>{selectedUser.username}</span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '150px 1fr',
                gap: '10px',
                padding: '10px',
                backgroundColor: '#f8f9fa',
                borderRadius: '4px'
              }}>
                <strong>Nombre completo:</strong>
                <span>{selectedUser.firstName} {selectedUser.lastName}</span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '150px 1fr',
                gap: '10px',
                padding: '10px'
              }}>
                <strong>Email:</strong>
                <span>{selectedUser.email}</span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '150px 1fr',
                gap: '10px',
                padding: '10px',
                backgroundColor: '#f8f9fa',
                borderRadius: '4px'
              }}>
                <strong>DNI:</strong>
                <span>{selectedUser.dni}</span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '150px 1fr',
                gap: '10px',
                padding: '10px'
              }}>
                <strong>Fecha de Nacimiento:</strong>
                <span>{selectedUser.dateOfBirth}</span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '150px 1fr',
                gap: '10px',
                padding: '10px',
                backgroundColor: '#f8f9fa',
                borderRadius: '4px'
              }}>
                <strong>Rol:</strong>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  backgroundColor: selectedUser.role === 'ADMIN' ? '#dc3545' : '#007bff',
                  color: 'white',
                  display: 'inline-block'
                }}>
                  {selectedUser.role}
                </span>
              </div>

              {selectedUser.createdAt && (
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '150px 1fr',
                  gap: '10px',
                  padding: '10px'
                }}>
                  <strong>Fecha de registro:</strong>
                  <span>{new Date(selectedUser.createdAt).toLocaleString()}</span>
                </div>
              )}
            </div>

            <div style={{
              display: 'flex',
              justifyContent: 'flex-end',
              marginTop: '25px'
            }}>
              <button
                onClick={() => setShowDetailsModal(false)}
                style={{
                  padding: '10px 20px',
                  borderRadius: '4px',
                  border: 'none',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  cursor: 'pointer'
                }}
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;
