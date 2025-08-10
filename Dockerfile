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

# Crear usuario no-root por seguridad
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001

# Instalar dependencias del sistema si son necesarias
RUN apk add --no-cache tzdata

# Crear directorio de la aplicación
WORKDIR /app

# Copiar el JAR compilado desde la etapa builder
COPY --from=builder /app/target/*.jar app.jar

# Cambiar la propiedad del archivo al usuario spring
RUN chown spring:spring app.jar

# Cambiar al usuario no-root
USER spring:spring

# Puerto que expone la aplicación (Railway usa la variable PORT)
EXPOSE 8080

# Variables de entorno por defecto - Optimizado para app pequeña
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/api/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

# ==============================================
# METADATOS (Documentación)
# ==============================================
LABEL maintainer="tu-email@dominio.com"
LABEL version="1.0.0"
LABEL description="User Management API - Spring Boot 3 + Java 21"
LABEL org.opencontainers.image.source="https://github.com/tu-usuario/user-management-api"
