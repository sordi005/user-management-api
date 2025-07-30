#!/bin/bash

# ===================================================================
# Reset de Base de Datos - User Management API
# ===================================================================

set -e

echo "🗑️  Reseteando base de datos..."

# Confirmar la acción
read -p "⚠️  ¿Estás seguro de resetear la base de datos? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Operación cancelada"
    exit 0
fi

# Parar la aplicación si está corriendo
echo "⏹️  Deteniendo servicios..."
docker-compose down

# Eliminar volúmenes de datos
echo "🧹 Eliminando datos persistentes..."
docker-compose down -v
docker volume prune -f

# Levantar PostgreSQL limpio
echo "🐳 Levantando PostgreSQL limpio..."
docker-compose up -d postgres

# Esperar a que esté listo
echo "⏳ Esperando PostgreSQL..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if docker-compose exec -T postgres pg_isready -U sordi005 -d user_management >/dev/null 2>&1; then
        echo "✅ PostgreSQL listo"
        break
    fi

    attempt=$((attempt + 1))
    echo "   Intento $attempt/$max_attempts..."
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ PostgreSQL no respondió"
    exit 1
fi

echo ""
echo "🎉 Base de datos reseteada exitosamente!"
echo ""
echo "📋 Próximos pasos:"
echo "   1. Ejecutar: ./mvnw spring-boot:run"
echo "   2. Flyway creará las tablas automáticamente"
echo ""
