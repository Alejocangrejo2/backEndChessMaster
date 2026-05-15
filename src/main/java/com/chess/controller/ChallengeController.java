package com.chess.controller;

import com.chess.model.Challenge;
import com.chess.service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de retos entre amigos.
 * 
 * Endpoints:
 * - POST /api/challenge/send       → Enviar reto (body: {to: "username"})
 * - GET  /api/challenge/pending     → Retos pendientes para el usuario actual
 * - GET  /api/challenge/sent        → Retos enviados por el usuario actual
 * - POST /api/challenge/{id}/accept → Aceptar reto
 * - POST /api/challenge/{id}/reject → Rechazar reto
 */
@RestController
@RequestMapping("/api/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    /**
     * POST /api/challenge/send
     * Enviar un reto a otro jugador.
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendChallenge(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String from = auth.getName();
        String to = body.get("to");
        
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Falta el usuario destino"));
        }

        if (from.equalsIgnoreCase(to)) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "No puedes retarte a ti mismo"));
        }

        Challenge challenge = challengeService.sendChallenge(from, to);
        return ResponseEntity.ok(challenge);
    }

    /**
     * GET /api/challenge/pending
     * Obtener retos pendientes (los que me enviaron).
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Challenge>> getPending(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(challengeService.getPendingFor(username));
    }

    /**
     * GET /api/challenge/sent
     * Obtener retos que yo envié (para ver si fueron aceptados).
     */
    @GetMapping("/sent")
    public ResponseEntity<List<Challenge>> getSent(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(challengeService.getSentBy(username));
    }

    /**
     * POST /api/challenge/{id}/accept
     * Aceptar un reto.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable String id, Authentication auth) {
        Challenge c = challengeService.accept(id);
        if (c == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Reto no encontrado o expirado"));
        }
        return ResponseEntity.ok(c);
    }

    /**
     * POST /api/challenge/{id}/reject
     * Rechazar un reto.
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id, Authentication auth) {
        Challenge c = challengeService.reject(id);
        if (c == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Reto no encontrado"));
        }
        return ResponseEntity.ok(c);
    }
}
