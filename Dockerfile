# ==============================================
# STAGE 1: BUILD (Compilación)
# ==============================================
# JDK completo + Maven para compilar
FROM eclipse-temurin:21-jdk-alpine AS builder

#Maven
RUN apk add --no-cache maven

# Crear directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para cache layers)
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Descargar dependencias (se cachea si no cambia pom.xml)
RUN mvn dependency:go-offline -B

# código fuente
COPY src ./src

# Compilar la aplicación (sin tests para ser más rápido)
RUN mvn clean package -DskipTests -B

# ==============================================
# STAGE 2: RUNTIME (Ejecución)
# ==============================================
# Imagen mucho más liviana, solo con JRE
FROM eclipse-temurin:21-jre-alpine

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

# Crear directorios necesarios
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde el stage de build
# --from=builder: copiar desde el stage anterior
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Variables de entorno por defecto para Railway
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# ¿Por qué exponemos el puerto?
# Es documentación + integración con orquestadores
EXPOSE 8080

# Healthcheck para Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# Comando de inicio
# dumb-init maneja señales correctamente (SIGTERM para shutdown graceful)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==============================================
# METADATOS (Documentación)
# ==============================================
LABEL maintainer="tu-email@dominio.com"
LABEL version="1.0.0"
LABEL description="User Management API - Spring Boot 3 + Java 21"
LABEL org.opencontainers.image.source="https://github.com/tu-usuario/user-management-api"
