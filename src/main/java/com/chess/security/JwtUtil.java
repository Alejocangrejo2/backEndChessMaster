package com.chess.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Clase utilitaria para manejar JWT (JSON Web Tokens).
 * 
 * Responsabilidades:
 * - Generar tokens JWT al hacer login
 * - Validar tokens JWT en cada request
 * - Extraer el username del token
 */
@Component
public class JwtUtil {

    /** Clave secreta para firmar los tokens */
    private final SecretKey secretKey;

    /** Tiempo de expiración del token (en milisegundos) */
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Genera un JWT token para un usuario.
     * 
     * @param username - Nombre de usuario (subject del token)
     * @return JWT token firmado
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrae el username (subject) de un token JWT.
     * 
     * @param token - JWT token
     * @return username del token
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Valida un token JWT.
     * Verifica la firma y que no haya expirado.
     * 
     * @param token - JWT token a validar
     * @return true si el token es válido
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parsea y valida las claims del token.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
