package com.chess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de CORS — permite TODOS los orígenes.
 * Esto es necesario para que Vercel pueda hablar con Railway.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir TODOS los orígenes (sin credenciales)
        configuration.setAllowedOrigins(List.of("*"));

        // Permitir todos los métodos HTTP (incluido OPTIONS para preflight)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Permitir todos los headers
        configuration.setAllowedHeaders(List.of("*"));

        // Exponer headers de respuesta
        configuration.setExposedHeaders(List.of("*"));

        // NO usar credentials con origen "*" (no es compatible)
        configuration.setAllowCredentials(false);

        // Caché del preflight: 1 hora
        configuration.setMaxAge(3600L);

        // Aplicar a TODAS las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}