package com.sordi.userManagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que intercepta todas las peticiones HTTP.
 *
 * Este filtro se ejecuta ANTES de que la petición llegue al controlador.
 * Su función es extraer y validar el token JWT, y si es válido,
 * establecer la autenticación en Spring Security.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor que recibe las dependencias necesarias.
     * Este constructor es llamado desde SecurityConfig.
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Método principal del filtro que se ejecuta en cada petición HTTP.
     *
     * @param request petición HTTP entrante
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Verificar si es un endpoint público - NO procesar JWT
            if (isPublicEndpoint(request)) {
                log.debug("Saltando procesamiento JWT para endpoint público: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // Extraer el token JWT del header Authorization
            String jwt = extractTokenFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                //Obtener el username del token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // Cargar los detalles del usuario
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                // Establecer detalles adicionales de la petición
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Establecer la autenticación en Spring Security
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Usuario autenticado: {}", username);
            }

        } catch (Exception ex) {
            // Si hay cualquier error, simplemente log y continuar sin autenticar
            log.error("Error al procesar token JWT: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        // SIEMPRE continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     *
     * @param request petición HTTP
     * @return token JWT sin el prefijo "Bearer ", o null si no existe
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Verificar si el header existe y tiene el formato correcto
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extraer solo el token, sin el prefijo "Bearer "
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Verifica si el endpoint es público y no requiere autenticación JWT.
     *
     * @param request petición HTTP
     * @return true si es un endpoint público, false si requiere autenticación
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.equals("/swagger-ui.html") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources/") ||
                path.startsWith("/webjars/") ||

                path.startsWith("/auth/") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/api/actuator/health") ||
                path.startsWith("/h2-console/");
    }
}
