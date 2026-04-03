package com.chess.controller;

import com.chess.dto.AnalysisRequestDTO;
import com.chess.dto.AnalysisResponseDTO;
import com.chess.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de análisis de partidas.
 *
 * Endpoints:
 * - POST /api/analysis/game → Analiza una partida completa
 * - GET  /api/analysis/health → Verifica disponibilidad del motor
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * POST /api/analysis/game
     * Analiza una partida completa y retorna evaluaciones + clasificaciones.
     *
     * Body: AnalysisRequestDTO con posiciones FEN y movimientos
     * Response: AnalysisResponseDTO con evaluaciones y precisión
     */
    @PostMapping("/game")
    public ResponseEntity<?> analyzeGame(@RequestBody AnalysisRequestDTO request) {
        try {
            AnalysisResponseDTO result = analysisService.analyzeGame(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Error durante el análisis: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analysis/health
     * Verifica si el motor de análisis está disponible.
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "engine", "stockfish",
            "message", "Motor de análisis disponible"
        ));
    }
}
