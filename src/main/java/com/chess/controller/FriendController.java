package com.chess.controller;

import com.chess.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST de amigos.
 *
 * Endpoints:
 * - GET    /api/friends/list           → Lista de amigos
 * - GET    /api/friends/requests       → Solicitudes pendientes
 * - GET    /api/friends/search?q=...   → Buscar usuarios
 * - POST   /api/friends/request        → Enviar solicitud
 * - POST   /api/friends/accept/{id}    → Aceptar solicitud
 * - POST   /api/friends/reject/{id}    → Rechazar solicitud
 * - DELETE /api/friends/{id}           → Eliminar amigo
 */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFriendsList(Authentication auth) {
        try {
            // Also register heartbeat when listing friends
            friendService.heartbeat(auth.getName());
            List<Map<String, Object>> friends = friendService.getFriendsList(auth.getName());
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(Authentication auth) {
        friendService.heartbeat(auth.getName());
        return ResponseEntity.ok(Map.of("status", "online"));
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests(Authentication auth) {
        try {
            List<Map<String, Object>> requests = friendService.getPendingRequests(auth.getName());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q, Authentication auth) {
        try {
            List<Map<String, Object>> results = friendService.searchUsers(q, auth.getName());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            String toUsername = body.get("toUsername");
            if (toUsername == null || toUsername.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username requerido"));
            }
            friendService.sendRequest(auth.getName(), toUsername);
            return ResponseEntity.ok(Map.of("message", "Solicitud enviada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id, Authentication auth) {
        try {
            friendService.acceptRequest(id, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Solicitud aceptada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, Authentication auth) {
        try {
            friendService.rejectRequest(id, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Solicitud rechazada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFriend(@PathVariable Long id, Authentication auth) {
        try {
            friendService.removeFriend(id, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Amigo eliminado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
