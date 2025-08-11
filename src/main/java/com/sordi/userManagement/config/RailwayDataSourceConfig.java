package com.sordi.userManagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuración profesional de DataSource para Railway
 *
 * Railway proporciona DATABASE_URL en formato: postgresql://user:pass@host:port/db
 * Spring Boot necesita: jdbc:postgresql://host:port/db
 *
 * Esta configuración maneja automáticamente la conversión sin requerir variables adicionales
 */
@Configuration
@Profile("prod")
public class RailwayDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            return createRailwayDataSource(databaseUrl);
        }

        // Fallback para desarrollo local (usa application-prod.yml)
        return DataSourceBuilder.create().build();
    }


    private DataSource createRailwayDataSource(String databaseUrl) {
        try {
            URI dbUri = new URI(databaseUrl);

            String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%d%s?sslmode=require",
                dbUri.getHost(),
                dbUri.getPort(),
                dbUri.getPath()
            );

            String[] userInfo = dbUri.getUserInfo().split(":");
            String username = userInfo[0];
            String password = userInfo[1];

            return DataSourceBuilder
                    .create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

        } catch (URISyntaxException e) {
            throw new RuntimeException("Error parsing DATABASE_URL from Railway: " + e.getMessage(), e);
        }
    }
}
