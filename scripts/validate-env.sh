#!/bin/bash

# ===================================================================
# Validador de Variables de Entorno - User Management API
# ===================================================================

set -e

echo "🔍 Validando variables de entorno..."

# Cargar variables del archivo .env si existe
if [ -f .env ]; then
    source .env
    echo "📄 Variables cargadas desde .env"
fi

# Lista de variables críticas requeridas
REQUIRED_VARS=(
    "DB_HOST"
    "DB_PORT"
    "DB_NAME"
    "DB_USERNAME"
    "DB_PASSWORD"
    "JWT_SECRET"
    "JWT_EXPIRATION_MS"
)

# Lista de variables opcionales con valores recomendados
OPTIONAL_VARS=(
    "LOG_LEVEL_APP:DEBUG"
    "LOG_LEVEL_SECURITY:INFO"
    "SHOW_SQL:false"
    "SERVER_PORT:8086"
    "SPRING_PROFILES_ACTIVE:dev"
)

# Validar variables críticas
echo ""
echo "🔑 Validando variables críticas:"
missing_vars=0

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ $var - NO CONFIGURADA"
        missing_vars=$((missing_vars + 1))
    else
        # Ocultar valores sensibles en la salida
        if [[ "$var" == *"PASSWORD"* ]] || [[ "$var" == *"SECRET"* ]]; then
            echo "✅ $var - CONFIGURADA (valor oculto)"
        else
            echo "✅ $var - ${!var}"
        fi
    fi
done

# Validar variables opcionales
echo ""
echo "⚙️  Variables opcionales:"
for var_def in "${OPTIONAL_VARS[@]}"; do
    var=$(echo $var_def | cut -d: -f1)
    default=$(echo $var_def | cut -d: -f2)

    if [ -z "${!var}" ]; then
        echo "⚠️  $var - Usando valor por defecto: $default"
    else
        echo "✅ $var - ${!var}"
    fi
done

# Validaciones específicas
echo ""
echo "🧪 Validaciones específicas:"

# Validar longitud del JWT secret
if [ ! -z "$JWT_SECRET" ]; then
    jwt_length=${#JWT_SECRET}
    if [ $jwt_length -lt 32 ]; then
        echo "❌ JWT_SECRET muy corto ($jwt_length caracteres). Mínimo recomendado: 32"
        missing_vars=$((missing_vars + 1))
    else
        echo "✅ JWT_SECRET tiene longitud adecuada ($jwt_length caracteres)"
    fi
fi

# Validar formato del puerto
if [ ! -z "$DB_PORT" ]; then
    if ! [[ "$DB_PORT" =~ ^[0-9]+$ ]] || [ "$DB_PORT" -lt 1 ] || [ "$DB_PORT" -gt 65535 ]; then
        echo "❌ DB_PORT inválido: $DB_PORT (debe ser 1-65535)"
        missing_vars=$((missing_vars + 1))
    else
        echo "✅ DB_PORT válido: $DB_PORT"
    fi
fi

# Resultado final
echo ""
if [ $missing_vars -eq 0 ]; then
    echo "🎉 ¡Todas las variables están configuradas correctamente!"
    echo ""
    echo "📋 Resumen de configuración:"
    echo "   - Base de datos: $DB_USERNAME@$DB_HOST:$DB_PORT/$DB_NAME"
    echo "   - Perfil activo: ${SPRING_PROFILES_ACTIVE:-dev}"
    echo "   - Puerto servidor: ${SERVER_PORT:-8086}"
    echo ""
    echo "✅ Listo para ejecutar: ./mvnw spring-boot:run"
    exit 0
else
    echo "❌ Se encontraron $missing_vars problema(s) de configuración"
    echo ""
    echo "🔧 Para solucionarlo:"
    echo "   1. Ejecuta: ./scripts/setup-dev-env.sh"
    echo "   2. O edita manualmente el archivo .env"
    echo "   3. Consulta la documentación en README.md"
    exit 1
fi
