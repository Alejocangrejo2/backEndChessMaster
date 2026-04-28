package com.chess.controller;

import com.chess.service.GameRoomService;
import com.chess.service.GameRoomService.GameRoom;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST para salas de juego multijugador.
 *
 * Endpoints:
 * - POST /api/room/create         -> Crear sala
 * - POST /api/room/join/{code}    -> Unirse
 * - GET  /api/room/{code}/state   -> Polling del estado
 * - POST /api/room/{code}/move    -> Hacer movimiento
 * - POST /api/room/{code}/end     -> Finalizar partida (mate, tablas, etc.)
 * - POST /api/room/{code}/resign  -> Rendirse
 */
@RestController
@RequestMapping("/api/room")
public class GameRoomController {

    private final GameRoomService roomService;

    public GameRoomController(GameRoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(Authentication auth) {
        try {
            String code = roomService.createRoom(auth.getName());
            return ResponseEntity.ok(Map.of("code", code, "status", "WAITING"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<?> joinRoom(@PathVariable String code, Authentication auth) {
        try {
            GameRoom room = roomService.joinRoom(code, auth.getName());
            return ResponseEntity.ok(room.toMap(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{code}/state")
    public ResponseEntity<?> getState(@PathVariable String code, Authentication auth) {
        try {
            GameRoom room = roomService.getRoom(code);
            return ResponseEntity.ok(room.toMap(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{code}/move")
    public ResponseEntity<?> makeMove(
            @PathVariable String code,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            String from = body.get("from");
            String to = body.get("to");
            String fen = body.get("fen");
            String san = body.getOrDefault("san", "");
            GameRoom room = roomService.makeMove(code, auth.getName(), from, to, fen, san);
            return ResponseEntity.ok(room.toMap(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/room/{code}/end
     * Finalizar partida con razon correcta.
     * Body: { "reason": "CHECKMATE|STALEMATE|DRAW", "winner": "white|black|null" }
     */
    @PostMapping("/{code}/end")
    public ResponseEntity<?> endGame(
            @PathVariable String code,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            String reason = body.getOrDefault("reason", "UNKNOWN");
            String winner = body.get("winner");
            GameRoom room = roomService.endGame(code, reason, winner);
            return ResponseEntity.ok(room.toMap(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/room/{code}/resign
     * Rendirse explicitamente.
     */
    @PostMapping("/{code}/resign")
    public ResponseEntity<?> resign(@PathVariable String code, Authentication auth) {
        try {
            GameRoom room = roomService.getRoom(code);
            String winner = auth.getName().equals(room.whitePlayer) ? "black" : "white";
            room = roomService.endGame(code, "RESIGNED", winner);
            return ResponseEntity.ok(room.toMap(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
