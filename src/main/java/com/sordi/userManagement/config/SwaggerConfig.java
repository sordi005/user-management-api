package com.sordi.userManagement.config;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para personalizar Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Agrupa los endpoints de autenticación
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-autenticacion")
                .displayName("🔐 Autenticación")
                .pathsToMatch("/auth/**")
                .build();
    }

    /**
     * Agrupa los endpoints de usuarios
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("02-usuarios")
                .displayName("👤 Gestión de Usuarios")
                .pathsToMatch("/users/**")
                .build();
    }

    /**
     * Agrupa los endpoints administrativos
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("03-administracion")
                .displayName("⚙️ Administración")
                .pathsToMatch("/admin/**")
                .build();
    }

    /**
     * API completa con todos los endpoints
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("00-api-completa")
                .displayName("📋 API Completa")
                .pathsToMatch("/**")
                .pathsToExclude("/actuator/**")
                .build();
    }

    /**
     * Personaliza la documentación OpenAPI con ejemplos y respuestas mejoradas
     */
    @Bean
    public OpenApiCustomizer customizeOpenApi() {
        return openApi -> {
            // Agregar ejemplos de respuestas de error comunes
            openApi.getPaths().values().forEach(pathItem -> {
                pathItem.readOperations().forEach(operation -> {
                    // Agregar respuesta 401 Unauthorized
                    operation.getResponses().addApiResponse("401",
                            new ApiResponse()
                                    .description("No autorizado - Token JWT inválido o faltante")
                                    .content(new Content()
                                            .addMediaType("application/json",
                                                    new MediaType()
                                                            .addExamples("unauthorized",
                                                                    new Example()
                                                                            .summary("Token inválido")
                                                                            .value("""
                                                                                    {
                                                                                      "success": false,
                                                                                      "message": "Token JWT inválido o expirado",
                                                                                      "data": null,
                                                                                      "error": "UNAUTHORIZED",
                                                                                      "status_code": 401,
                                                                                      "time_stamp": "2025-08-13T10:30:00Z"
                                                                                    }
                                                                                    """)
                                                            )
                                            )
                                    )
                    );

                    // Agregar respuesta 403 Forbidden
                    operation.getResponses().addApiResponse("403",
                            new ApiResponse()
                                    .description("Prohibido - Sin permisos suficientes")
                                    .content(new Content()
                                            .addMediaType("application/json",
                                                    new MediaType()
                                                            .addExamples("forbidden",
                                                                    new Example()
                                                                            .summary("Sin permisos")
                                                                            .value("""
                                                                                    {
                                                                                      "success": false,
                                                                                      "message": "No tienes permisos para acceder a este recurso",
                                                                                      "data": null,
                                                                                      "error": "FORBIDDEN",
                                                                                      "status_code": 403,
                                                                                      "time_stamp": "2025-08-13T10:30:00Z"
                                                                                    }
                                                                                    """)
                                                            )
                                            )
                                    )
                    );

                    // Agregar respuesta 500 Internal Server Error
                    operation.getResponses().addApiResponse("500",
                            new ApiResponse()
                                    .description("Error interno del servidor")
                                    .content(new Content()
                                            .addMediaType("application/json",
                                                    new MediaType()
                                                            .addExamples("server_error",
                                                                    new Example()
                                                                            .summary("Error interno")
                                                                            .value("""
                                                                                    {
                                                                                      "success": false,
                                                                                      "message": "Error interno del servidor",
                                                                                      "data": null,
                                                                                      "error": "INTERNAL_SERVER_ERROR",
                                                                                      "status_code": 500,
                                                                                      "time_stamp": "2025-08-13T10:30:00Z"
                                                                                    }
                                                                                    """)
                                                            )
                                            )
                                    )
                    );
                });
            });
        };
    }
}
