package com.chess.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint para Railway y monitoreo.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<?> root() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ChessMaster Backend",
            "version", "1.0.0"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
