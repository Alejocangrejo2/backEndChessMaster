package com.chess.controller;

import com.chess.dto.GameStateDTO;
import com.chess.dto.MoveRequestDTO;
import com.chess.dto.NewGameRequestDTO;
import com.chess.model.User;
import com.chess.service.GameService;
import com.chess.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de partidas de ajedrez.
 * PATRÓN MVC: Capa de presentación para el juego.
 * 
 * Endpoints (protegidos por JWT):
 * - POST /api/game/new → Crea nueva partida
 * - POST /api/game/{id}/move → Registra un movimiento
 * - GET  /api/game/{id}/state → Retorna estado actual
 * - GET  /api/game/history → Historial del usuario
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    public GameController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    /**
     * POST /api/game/new
     * Crea una nueva partida de ajedrez.
     * 
     * Body: { "difficulty": "easy" | "medium" | "hard" }
     * Response: GameStateDTO con el estado inicial
     */
    @PostMapping("/new")
    public ResponseEntity<?> createGame(
            @RequestBody NewGameRequestDTO request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User player = userService.findByUsername(username);
            GameStateDTO game = gameService.createGame(request, player);
            return ResponseEntity.ok(game);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/game/{id}/move
     * Registra un movimiento en una partida existente.
     * 
     * Body: { "fromSquare": "e2", "toSquare": "e4" }
     * Response: GameStateDTO actualizado
     */
    @PostMapping("/{id}/move")
    public ResponseEntity<?> makeMove(
            @PathVariable Long id,
            @Valid @RequestBody MoveRequestDTO request) {
        try {
            GameStateDTO state = gameService.makeMove(id, request);
            return ResponseEntity.ok(state);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/game/{id}/state
     * Retorna el estado actual de una partida.
     * 
     * Response: GameStateDTO completo
     */
    @GetMapping("/{id}/state")
    public ResponseEntity<?> getGameState(@PathVariable Long id) {
        try {
            GameStateDTO state = gameService.getGameState(id);
            return ResponseEntity.ok(state);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/game/history
     * Retorna el historial de partidas del usuario autenticado.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            String username = authentication.getName();
            User player = userService.findByUsername(username);
            List<GameStateDTO> games = gameService.getPlayerGames(player.getId());
            return ResponseEntity.ok(games);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
