package com.sordi.userManagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración profesional de OpenAPI/Swagger para la API de gestión de usuarios.
 * Esta configuración proporciona una documentación completa y profesional de la API.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI apiInfo() {
        // Nombre del esquema de seguridad JWT
        String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, createJwtSecurityScheme())
                );
    }

    /**
     * Crea la información general de la API
     */
    private Info createApiInfo() {
        return new Info()
                .title("API de Gestión de Usuarios")
                .version("1.0.0")
                .description("""
                        API RESTful para la gestión integral de usuarios con autenticación JWT.
                        
                        ## Características principales:
                        - ✅ Autenticación y autorización con JWT
                        - ✅ Gestión completa de usuarios (CRUD)
                        - ✅ Sistema de roles (USER, ADMIN)
                        - ✅ Registro y login de usuarios
                        - ✅ Renovación de tokens (refresh)
                        - ✅ Endpoints administrativos
                        
                        ## Cómo usar esta API:
                        1. **Registrarse**: POST `/auth/register` para crear una cuenta
                        2. **Iniciar sesión**: POST `/auth/login` para obtener token JWT
                        3. **Usar token**: Incluir `Authorization: Bearer {token}` en las peticiones
                        4. **Renovar token**: POST `/auth/refresh` cuando el token expire
                        
                        ## Roles disponibles:
                        - **USER**: Acceso a endpoints básicos de usuario
                        - **ADMIN**: Acceso completo a todos los endpoints
                        """)
                .contact(new Contact()
                        .name("Santiago Sordi")
                        .email("sordisantaigo5@gmail.com")
                        .url("https://github.com/sordi005/user-management-api.git"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Configura los servidores disponibles
     */
    private List<Server> createServers() {
        String baseUrl = "http://localhost:" + serverPort;
        if (contextPath != null && !contextPath.isEmpty()) {
            baseUrl += contextPath;
        }

        return List.of(
                new Server()
                        .url(baseUrl)
                        .description("Servidor de desarrollo local"),
                new Server()
                        .url("https://your-production-domain.com")
                        .description("Servidor de producción")
        );
    }

    /**
     * Crea el esquema de seguridad JWT
     */
    private SecurityScheme createJwtSecurityScheme() {
        return new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        **Autenticación JWT (JSON Web Token)**
                        
                        Para usar los endpoints protegidos:
                        1. Obtén un token haciendo login en `/auth/login`
                        2. Incluye el token en el header: `Authorization: Bearer {tu_token}`
                        3. El token expira en 24 horas por defecto
                        4. Usa `/auth/refresh` para renovar el token
                        
                        **Ejemplo:**
                        ```
                        Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                        ```
                        """);
    }
}
