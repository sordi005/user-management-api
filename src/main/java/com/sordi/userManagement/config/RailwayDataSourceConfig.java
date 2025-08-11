package com.sordi.userManagement.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuración  de DataSource para Railway
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
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            return createRailwayDataSource(databaseUrl);
        }

        // Fallback para desarrollo local
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/user_management?sslmode=disable&ApplicationName=user-management-api");
        dataSource.setUsername("user");
        dataSource.setPassword("1234");
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }

    /**
     * Configuración de Flyway para gestionar migraciones de base de datos
     * Esta configuración se aplica en producción
     * y utiliza el DataSource proporcionado por Railway
     * para realizar las migraciones de esquema.
     * @param dataSource
     * @return Flyway instance
     */
    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .table("flyway_schema_history")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load();
    }

    /**
     * Crea un DataSource a partir de la URL proporcionada por Railway
     * La URL debe estar en el formato: postgresql://user:pass@host:port/db
     * @param databaseUrl La URL de la base de datos proporcionada por Railway
     * @return DataSource configurado para conectarse a la base de datos
     */
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

            // Crear HikariDataSource manualmente con configuración completa
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName("org.postgresql.Driver");

            // Configuración de pool para Railway
            dataSource.setMaximumPoolSize(3);
            dataSource.setMinimumIdle(1);
            dataSource.setConnectionTimeout(10000);
            dataSource.setIdleTimeout(180000);
            dataSource.setMaxLifetime(600000);

            return dataSource;

        } catch (URISyntaxException e) {
            throw new RuntimeException("Error parsing DATABASE_URL from Railway: " + e.getMessage(), e);
        }
    }
}
