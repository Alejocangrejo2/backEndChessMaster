package com.chess.config;

import com.chess.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security.
 *
 * - Deshabilita CSRF (API REST stateless)
 * - CORS habilitado (usa CorsConfigurationSource bean)
 * - Sesión STATELESS (JWT)
 * - OPTIONS permitido globalmente (preflight CORS)
 * - /api/auth/** público
 * - /api/game/** protegido con JWT
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (API REST stateless)
            .csrf(csrf -> csrf.disable())

            // Habilitar CORS (usa el bean CorsConfigurationSource)
            .cors(Customizer.withDefaults())

            // Sesión stateless (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // IMPORTANTE: Permitir ALL preflight OPTIONS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Endpoints públicos (login, register, analysis)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/analysis/**").permitAll()
                // Endpoints protegidos (requieren JWT)
                .requestMatchers("/api/game/**").authenticated()
                .requestMatchers("/api/friends/**").authenticated()
                // Cualquier otro request es permitido
                .anyRequest().permitAll()
            )

            // Agregar filtro JWT antes del filtro de autenticación de Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt para hashear contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
