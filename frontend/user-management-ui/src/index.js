/**
 * PUNTO DE ENTRADA DE LA APLICACIÓN REACT
 *
 * Archivo principal que conecta App.js con DOM
 * Renderiza la aplicación en el div #root del index.html
 */

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// Obtener el elemento root del DOM donde se montará React
const root = ReactDOM.createRoot(document.getElementById('root'));

// Renderizar la aplicación principal
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
