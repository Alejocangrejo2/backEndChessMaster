package com.chess.dto;

import java.util.List;

/**
 * DTO para solicitar análisis de una partida.
 * Recibe la lista de posiciones FEN y movimientos.
 */
public class AnalysisRequestDTO {

    /** Lista de posiciones FEN: [posición_inicial, después_mov_1, ...] */
    private List<String> positions;

    /** Lista de movimientos en formato simple */
    private List<MoveData> moves;

    public List<String> getPositions() { return positions; }
    public void setPositions(List<String> positions) { this.positions = positions; }
    public List<MoveData> getMoves() { return moves; }
    public void setMoves(List<MoveData> moves) { this.moves = moves; }

    /**
     * Datos de un movimiento individual.
     */
    public static class MoveData {
        private String san;
        private String from;
        private String to;
        private String color; // "white" or "black"

        public String getSan() { return san; }
        public void setSan(String san) { this.san = san; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}
