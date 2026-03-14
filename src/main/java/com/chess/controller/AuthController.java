package com.chess.controller;

import com.chess.dto.AuthResponseDTO;
import com.chess.dto.LoginRequestDTO;
import com.chess.dto.RegisterRequestDTO;
import com.chess.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticación.
 * PATRÓN MVC: Capa de presentación para login y registro.
 * 
 * Endpoints:
 * - POST /api/auth/register → Registro de nuevo usuario
 * - POST /api/auth/login → Login y obtención de JWT
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/auth/register
     * Registra un nuevo usuario y retorna JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            AuthResponseDTO response = userService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login
     * Autentica un usuario y retorna JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            AuthResponseDTO response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
