package com.chess.service;

import com.chess.dto.AnalysisRequestDTO;
import com.chess.dto.AnalysisResponseDTO;
import com.chess.dto.AnalysisResponseDTO.MoveAnalysisDTO;
import com.chess.dto.AnalysisResponseDTO.ClassificationCounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de análisis de partidas con Stockfish.
 *
 * Evalúa todas las posiciones de una partida, clasifica cada jugada
 * en 8 categorías (tipo chess.com), y calcula la precisión de cada jugador.
 *
 * TODAS las evaluaciones son desde la perspectiva de BLANCAS:
 * - Positivo = ventaja blancas
 * - Negativo = ventaja negras
 */
@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final StockfishProcess stockfish;

    public AnalysisService(StockfishProcess stockfish) {
        this.stockfish = stockfish;
    }

    /**
     * Analiza una partida completa.
     *
     * @param request Posiciones FEN + movimientos
     * @return Análisis con evaluaciones, clasificaciones y precisión
     */
    public AnalysisResponseDTO analyzeGame(AnalysisRequestDTO request) {
        List<String> positions = request.getPositions();
        List<AnalysisRequestDTO.MoveData> moves = request.getMoves();

        if (positions == null || moves == null || positions.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 posiciones para analizar");
        }

        // Step 1: Evaluate ALL positions (scores from white's perspective)
        List<Integer> evaluations = new ArrayList<>();
        for (String fen : positions) {
            if (stockfish.isAvailable()) {
                StockfishProcess.EvalResult result = stockfish.evaluate(fen, 16, 2000);
                evaluations.add(result.getScore());
            } else {
                evaluations.add(0); // Fallback if Stockfish unavailable
            }
        }

        // Step 2: Analyze each move
        List<MoveAnalysisDTO> moveAnalyses = new ArrayList<>();
        List<Integer> whiteCpLosses = new ArrayList<>();
        List<Integer> blackCpLosses = new ArrayList<>();

        for (int i = 0; i < moves.size(); i++) {
            AnalysisRequestDTO.MoveData move = moves.get(i);
            int evalBefore = i < evaluations.size() ? evaluations.get(i) : 0;
            int evalAfter = (i + 1) < evaluations.size() ? evaluations.get(i + 1) : 0;

            // Get best move for this position
            String bestMoveUci = "";
            if (stockfish.isAvailable() && i < positions.size()) {
                StockfishProcess.EvalResult best = stockfish.evaluate(positions.get(i), 16, 2000);
                bestMoveUci = best.getBestMove();
            }

            // Check if player's move matches best
            String playerMoveUci = move.getFrom() + move.getTo();
            boolean isBestMove = bestMoveUci.startsWith(playerMoveUci);

            // Classify
            String color = move.getColor();
            String classification = classifyMove(evalBefore, evalAfter, isBestMove, color, i);

            // Calculate centipawn loss
            int cpLoss;
            if ("white".equals(color)) {
                cpLoss = Math.max(0, evalBefore - evalAfter);
            } else {
                cpLoss = Math.max(0, evalAfter - evalBefore);
            }

            // Track losses per side
            if ("white".equals(color)) {
                whiteCpLosses.add(cpLoss);
            } else {
                blackCpLosses.add(cpLoss);
            }

            MoveAnalysisDTO dto = new MoveAnalysisDTO();
            dto.setMoveIndex(i);
            dto.setSan(move.getSan());
            dto.setFrom(move.getFrom());
            dto.setTo(move.getTo());
            dto.setColor(color);
            dto.setEvalBefore(evalBefore);
            dto.setEvalAfter(evalAfter);
            dto.setBestMove(bestMoveUci);
            dto.setClassification(classification);
            dto.setCpLoss(cpLoss);

            moveAnalyses.add(dto);
        }

        // Step 3: Build response
        AnalysisResponseDTO response = new AnalysisResponseDTO();
        response.setMoves(moveAnalyses);
        response.setWhiteAccuracy(calculateAccuracy(whiteCpLosses));
        response.setBlackAccuracy(calculateAccuracy(blackCpLosses));
        response.setWhiteCounts(countClassifications(moveAnalyses, "white"));
        response.setBlackCounts(countClassifications(moveAnalyses, "black"));

        log.info("Analysis complete: {} moves, white={}%, black={}%",
            moveAnalyses.size(), response.getWhiteAccuracy(), response.getBlackAccuracy());

        return response;
    }

    /**
     * Classify a move into one of 8 categories.
     * Logic mirrors the frontend AnalysisEngine exactly.
     */
    private String classifyMove(int evalBefore, int evalAfter, boolean isBestMove, String color, int moveNumber) {
        // Opening moves — less strict
        if (moveNumber <= 10) {
            if (isBestMove) return "best";
            int loss = "white".equals(color) ? evalBefore - evalAfter : evalAfter - evalBefore;
            if (loss <= 10) return "book";
            if (loss <= 30) return "good";
            if (loss <= 80) return "inaccuracy";
            if (loss <= 200) return "mistake";
            return "blunder";
        }

        int cpLoss = "white".equals(color)
            ? evalBefore - evalAfter
            : evalAfter - evalBefore;

        // Brilliant: was losing, now winning (not obvious best)
        boolean wasLosing = "white".equals(color) ? evalBefore < -50 : evalBefore > 50;
        boolean nowWinning = "white".equals(color) ? evalAfter > 150 : evalAfter < -150;
        if (!isBestMove && wasLosing && nowWinning && cpLoss <= 0) return "brilliant";

        // Great: found the right move in a losing position
        if (isBestMove && wasLosing && nowWinning) return "great";

        // Best move
        if (isBestMove && cpLoss <= 5) return "best";

        // Good
        if (cpLoss <= 30) return "good";

        // Missed opportunity
        boolean hadWinning = "white".equals(color) ? evalBefore > 200 : evalBefore < -200;
        boolean isNowNeutral = Math.abs(evalAfter) < 100;
        if (hadWinning && isNowNeutral && cpLoss > 100) return "missed";

        // Inaccuracy
        if (cpLoss <= 100) return "inaccuracy";

        // Mistake
        if (cpLoss <= 300) return "mistake";

        // Blunder
        return "blunder";
    }

    /**
     * Calculate accuracy using chess.com-like formula.
     * accuracy = 103.1668 * e^(-0.04354 * avgCpLoss) - 3.1668
     */
    private double calculateAccuracy(List<Integer> cpLosses) {
        if (cpLosses.isEmpty()) return 100.0;
        double avgLoss = cpLosses.stream().mapToInt(Integer::intValue).average().orElse(0);
        double accuracy = 103.1668 * Math.exp(-0.04354 * avgLoss) - 3.1668;
        return Math.round(Math.max(0, Math.min(100, accuracy)) * 10.0) / 10.0;
    }

    /**
     * Count classification types for one color.
     */
    private ClassificationCounts countClassifications(List<MoveAnalysisDTO> moves, String color) {
        ClassificationCounts counts = new ClassificationCounts();
        for (MoveAnalysisDTO m : moves) {
            if (!color.equals(m.getColor())) continue;
            switch (m.getClassification()) {
                case "brilliant":  counts.setBrilliant(counts.getBrilliant() + 1); break;
                case "great":      counts.setGreat(counts.getGreat() + 1); break;
                case "best":       counts.setBest(counts.getBest() + 1); break;
                case "good":       counts.setGood(counts.getGood() + 1); break;
                case "inaccuracy": counts.setInaccuracy(counts.getInaccuracy() + 1); break;
                case "mistake":    counts.setMistake(counts.getMistake() + 1); break;
                case "blunder":    counts.setBlunder(counts.getBlunder() + 1); break;
            }
        }
        return counts;
    }
}
