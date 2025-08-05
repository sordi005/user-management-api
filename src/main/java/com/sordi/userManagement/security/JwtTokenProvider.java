package com.sordi.userManagement.security;

import com.sordi.userManagement.config.JwtConfig;
import com.sordi.userManagement.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Proveedor de tokens JWT.
 * Maneja la creación, validación y extracción de información de tokens JWT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    /**
     * Configuración JWT inyectada desde JwtConfig
     */
    private final JwtConfig jwtConfig;

    /**
     * Clave secreta convertida a SecretKey para mayor seguridad.
     * Se inicializa en el método init() usando la configuración de JwtConfig.
     */
    private SecretKey secretKey;

    /**
     * Inicializa la clave secreta después de la construcción del bean.
     * Convierte la clave string en SecretKey para mayor seguridad criptográfica.
     */
    @PostConstruct
    public void init() {
        try {
            // Convierte la clave string de JwtConfig en SecretKey segura
            this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
            log.info("JwtTokenProvider inicializado correctamente");
        } catch (Exception e) {
            log.error("Error inicializando JwtTokenProvider: {}", e.getMessage());
            throw new IllegalArgumentException("Error inicializando el proveedor de tokens JWT");
        }
    }

    /**
     * Valida si un token JWT es válido y no ha expirado.
     *
     * @param token token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Intento de validación con token vacío");
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (SecurityException e) {
            log.warn("Token JWT con firma inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT con claims vacíos: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado validando token JWT: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Genera un token JWT para el usuario especificado.
     *
     * @param username nombre de usuario para incluir en el token
     * @return token JWT firmado como String
     */
    public String generateToken(String username) {
        // Validación de entrada
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede generar token sin username");
        }
        try {
            Date now = new Date();
            Date expiryDate = jwtConfig.calculateExpirationDate();

            //Construir el token
            return Jwts.builder()
                    .subject(username)                      // "sub": username
                    .issuedAt(now)                         // "iat": fecha actual
                    .expiration(expiryDate)                // "exp": fecha expiración
                    .issuer("user-management-api")         // "iss": emisor
                    // SIGNATURE (firma digital)
                    .signWith(secretKey)
                    .compact();  // Convierte a string JWT final

        } catch (Exception e) {
            log.error("Error generando token JWT para usuario {}: {}", username, e.getMessage());
            throw new RuntimeException("Error generando token de autenticación");
        }
    }

    /**
     * Extrae el username del token JWT.
     *
     * @param token token JWT válido
     * @return username contenido en el token
     */
    public String getUsernameFromToken(String token) {
        if (!validateToken(token)) {
            throw new BusinessException("Token JWT inválido");
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();  // Obtiene los claims del token(UserName, issuedAt, expiration, etc.)

            String username = claims.getSubject();
            if (username == null || username.trim().isEmpty()) {
                throw new BusinessException("Token JWT no contiene username válido");
            }

            return username;

        } catch (Exception e) {
            log.error("Error extrayendo username del token: {}", e.getMessage());
            throw new BusinessException("Error procesando token de autenticación");
        }
    }

    /**
     * Genera un refresh token para el usuario especificado.
     *
     * @param username nombre de usuario para incluir en el refresh token
     * @return refresh token JWT firmado
     */
    public String generateRefreshToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede generar refresh token sin username");
        }

        try {
            Date now = new Date();
            Date expiryDate = jwtConfig.calculateRefreshExpirationDate();

            return Jwts.builder()
                    .subject(username)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .issuer("user-management-api")
                    .claim("type", "refresh") // Marca como refresh token
                    .signWith(secretKey)
                    .compact();

        } catch (Exception e) {
            log.error("Error generando refresh token para usuario {}: {}", username, e.getMessage());
            throw new RuntimeException("Error generando refresh token");
        }
    }

    /**
     * Obtiene la fecha de expiración del token.
     *
     * @param token token JWT válido
     * @return fecha de expiración del token
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return claims.getExpiration();

        } catch (Exception e) {
            log.error("Error obteniendo fecha de expiración del token: {}", e.getMessage());
            throw new BusinessException("Error procesando token de autenticación");
        }
    }

    /**
     * Valida si un refresh token es válido y es realmente un refresh token.
     *
     * @param refreshToken token de refresco a validar
     * @return true si el refresh token es válido, false en caso contrario
     */
    public boolean validateRefreshToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            return false;
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

            // Verificar que sea un refresh token
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);

        } catch (Exception e) {
            log.warn("Error validando refresh token: {}", e.getMessage());
            return false;
        }
    }
}
