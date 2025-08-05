@echo off
REM ===================================================================
REM Reset de Base de Datos - User Management API (Windows)
REM ===================================================================

echo 🗑️  Reseteando base de datos...

REM Confirmar la acción
set /p confirm="⚠️  ¿Estás seguro de resetear la base de datos? (y/N): "
if /i not "%confirm%"=="y" (
    echo ❌ Operación cancelada
    exit /b 0
)

REM Parar la aplicación si está corriendo
echo ⏹️  Deteniendo servicios...
docker-compose down

REM Eliminar volúmenes de datos
echo 🧹 Eliminando datos persistentes...
docker-compose down -v
docker volume prune -f

REM Levantar PostgreSQL limpio
echo 🐳 Levantando PostgreSQL limpio...
docker-compose up -d postgres

REM Esperar a que esté listo
echo ⏳ Esperando PostgreSQL...
timeout /t 10 /nobreak >nul

echo.
echo 🎉 Base de datos reseteada exitosamente!
echo.
echo 📋 Próximos pasos:
echo    1. Ejecutar: mvnw spring-boot:run
echo    2. Flyway creará las tablas automáticamente
echo.
