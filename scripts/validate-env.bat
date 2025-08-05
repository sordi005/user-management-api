@echo off
REM ===================================================================
REM Validador de Variables de Entorno - User Management API (Windows)
REM ===================================================================

echo 🔍 Validando variables de entorno...

REM Cargar variables del archivo .env si existe
if exist .env (
    echo 📄 Cargando variables desde .env...
    for /f "delims=" %%x in (.env) do (set "%%x")
)

echo.
echo 🔑 Validando variables críticas:

REM Variables críticas
set missing_vars=0

if "%DB_HOST%"=="" (
    echo ❌ DB_HOST - NO CONFIGURADA
    set /a missing_vars+=1
) else (
    echo ✅ DB_HOST - %DB_HOST%
)

if "%DB_PORT%"=="" (
    echo ❌ DB_PORT - NO CONFIGURADA
    set /a missing_vars+=1
) else (
    echo ✅ DB_PORT - %DB_PORT%
)

if "%DB_NAME%"=="" (
    echo ❌ DB_NAME - NO CONFIGURADA
    set /a missing_vars+=1
) else (
    echo ✅ DB_NAME - %DB_NAME%
)

if "%DB_USERNAME%"=="" (
    echo ❌ DB_USERNAME - NO CONFIGURADA
    set /a missing_vars+=1
) else (
    echo ✅ DB_USERNAME - %DB_USERNAME%
)

if "%DB_PASSWORD%"=="" (
    echo ❌ DB_PASSWORD - NO CONFIGURADA
    set /a missing_vars+=1
) else (
    echo ✅ DB_PASSWORD - CONFIGURADA (valor oculto)
)

if "%JWT_SECRET%"=="" (
    echo ❌ JWT_SECRET - NO CONFIGURADA
    set /a missing_vars+=1
) else (
    echo ✅ JWT_SECRET - CONFIGURADA (valor oculto)
)

if "%JWT_EXPIRATION_MS%"=="" (
    echo ❌ JWT_EXPIRATION_MS - NO CONFIGURADA
    set /a missing_vars+=1
) else (
    echo ✅ JWT_EXPIRATION_MS - %JWT_EXPIRATION_MS%
)

echo.
echo ⚙️  Variables opcionales:
if "%LOG_LEVEL_APP%"=="" (
    echo ⚠️  LOG_LEVEL_APP - Usando valor por defecto: DEBUG
) else (
    echo ✅ LOG_LEVEL_APP - %LOG_LEVEL_APP%
)

if "%SERVER_PORT%"=="" (
    echo ⚠️  SERVER_PORT - Usando valor por defecto: 8086
) else (
    echo ✅ SERVER_PORT - %SERVER_PORT%
)

if "%SPRING_PROFILES_ACTIVE%"=="" (
    echo ⚠️  SPRING_PROFILES_ACTIVE - Usando valor por defecto: dev
) else (
    echo ✅ SPRING_PROFILES_ACTIVE - %SPRING_PROFILES_ACTIVE%
)

echo.
if %missing_vars%==0 (
    echo 🎉 ¡Todas las variables están configuradas correctamente!
    echo.
    echo 📋 Resumen de configuración:
    echo    - Base de datos: %DB_USERNAME%@%DB_HOST%:%DB_PORT%/%DB_NAME%
    echo    - Perfil activo: %SPRING_PROFILES_ACTIVE%
    echo    - Puerto servidor: %SERVER_PORT%
    echo.
    echo ✅ Listo para ejecutar: mvnw spring-boot:run
) else (
    echo ❌ Se encontraron %missing_vars% problema(s) de configuración
    echo.
    echo 🔧 Para solucionarlo:
    echo    1. Crea/edita el archivo .env
    echo    2. Consulta la documentación en README.md
)

pause
