package com.chess.dto;

import java.util.List;

/**
 * DTO — Estado del juego como respuesta de la API.
 * Incluye tablero, turno, estado, y lista de movimientos.
 */
public class GameStateDTO {

    private Long gameId;
    private String boardState;
    private String status;
    private String currentTurn;
    private String difficulty;
    private String winner;
    private List<MoveDTO> moves;

    public GameStateDTO() {}

    // === Getters y Setters ===

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public String getBoardState() { return boardState; }
    public void setBoardState(String boardState) { this.boardState = boardState; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public List<MoveDTO> getMoves() { return moves; }
    public void setMoves(List<MoveDTO> moves) { this.moves = moves; }

    /**
     * DTO interno para representar un movimiento en la respuesta.
     */
    public static class MoveDTO {
        private Integer moveNumber;
        private String fromSquare;
        private String toSquare;
        private String pieceType;
        private String pieceColor;
        private String captured;
        private String notation;

        public MoveDTO() {}

        public MoveDTO(Integer moveNumber, String fromSquare, String toSquare,
                       String pieceType, String pieceColor, String captured, String notation) {
            this.moveNumber = moveNumber;
            this.fromSquare = fromSquare;
            this.toSquare = toSquare;
            this.pieceType = pieceType;
            this.pieceColor = pieceColor;
            this.captured = captured;
            this.notation = notation;
        }

        public Integer getMoveNumber() { return moveNumber; }
        public void setMoveNumber(Integer moveNumber) { this.moveNumber = moveNumber; }

        public String getFromSquare() { return fromSquare; }
        public void setFromSquare(String fromSquare) { this.fromSquare = fromSquare; }

        public String getToSquare() { return toSquare; }
        public void setToSquare(String toSquare) { this.toSquare = toSquare; }

        public String getPieceType() { return pieceType; }
        public void setPieceType(String pieceType) { this.pieceType = pieceType; }

        public String getPieceColor() { return pieceColor; }
        public void setPieceColor(String pieceColor) { this.pieceColor = pieceColor; }

        public String getCaptured() { return captured; }
        public void setCaptured(String captured) { this.captured = captured; }

        public String getNotation() { return notation; }
        public void setNotation(String notation) { this.notation = notation; }
    }
}
