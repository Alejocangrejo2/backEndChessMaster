package com.chess.dto;

import java.util.List;

/**
 * DTO de respuesta del análisis de partida.
 * Contiene evaluaciones, clasificaciones y precisión.
 */
public class AnalysisResponseDTO {

    private List<MoveAnalysisDTO> moves;
    private double whiteAccuracy;
    private double blackAccuracy;
    private ClassificationCounts whiteCounts;
    private ClassificationCounts blackCounts;

    // Getters & Setters
    public List<MoveAnalysisDTO> getMoves() { return moves; }
    public void setMoves(List<MoveAnalysisDTO> moves) { this.moves = moves; }
    public double getWhiteAccuracy() { return whiteAccuracy; }
    public void setWhiteAccuracy(double whiteAccuracy) { this.whiteAccuracy = whiteAccuracy; }
    public double getBlackAccuracy() { return blackAccuracy; }
    public void setBlackAccuracy(double blackAccuracy) { this.blackAccuracy = blackAccuracy; }
    public ClassificationCounts getWhiteCounts() { return whiteCounts; }
    public void setWhiteCounts(ClassificationCounts whiteCounts) { this.whiteCounts = whiteCounts; }
    public ClassificationCounts getBlackCounts() { return blackCounts; }
    public void setBlackCounts(ClassificationCounts blackCounts) { this.blackCounts = blackCounts; }

    /**
     * Análisis de un movimiento individual.
     */
    public static class MoveAnalysisDTO {
        private int moveIndex;
        private String san;
        private String from;
        private String to;
        private String color;
        private int evalBefore;    // Centipawns desde perspectiva blancas
        private int evalAfter;
        private String bestMove;   // UCI format
        private String classification;
        private int cpLoss;

        // Getters & Setters
        public int getMoveIndex() { return moveIndex; }
        public void setMoveIndex(int moveIndex) { this.moveIndex = moveIndex; }
        public String getSan() { return san; }
        public void setSan(String san) { this.san = san; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public int getEvalBefore() { return evalBefore; }
        public void setEvalBefore(int evalBefore) { this.evalBefore = evalBefore; }
        public int getEvalAfter() { return evalAfter; }
        public void setEvalAfter(int evalAfter) { this.evalAfter = evalAfter; }
        public String getBestMove() { return bestMove; }
        public void setBestMove(String bestMove) { this.bestMove = bestMove; }
        public String getClassification() { return classification; }
        public void setClassification(String classification) { this.classification = classification; }
        public int getCpLoss() { return cpLoss; }
        public void setCpLoss(int cpLoss) { this.cpLoss = cpLoss; }
    }

    /**
     * Conteo de clasificaciones por lado.
     */
    public static class ClassificationCounts {
        private int brilliant;
        private int great;
        private int best;
        private int good;
        private int inaccuracy;
        private int mistake;
        private int blunder;

        // Getters & Setters
        public int getBrilliant() { return brilliant; }
        public void setBrilliant(int brilliant) { this.brilliant = brilliant; }
        public int getGreat() { return great; }
        public void setGreat(int great) { this.great = great; }
        public int getBest() { return best; }
        public void setBest(int best) { this.best = best; }
        public int getGood() { return good; }
        public void setGood(int good) { this.good = good; }
        public int getInaccuracy() { return inaccuracy; }
        public void setInaccuracy(int inaccuracy) { this.inaccuracy = inaccuracy; }
        public int getMistake() { return mistake; }
        public void setMistake(int mistake) { this.mistake = mistake; }
        public int getBlunder() { return blunder; }
        public void setBlunder(int blunder) { this.blunder = blunder; }
    }
}
