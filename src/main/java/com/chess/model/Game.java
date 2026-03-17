package com.chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA — Partida de ajedrez.
 * 
 * Contiene el estado del tablero como JSON (TEXT),
 * las relaciones con jugadores, y el historial de movimientos.
 * 
 * PATRÓN REPOSITORY: Se accede a través de GameRepository.
 */
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Jugador de blancas */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "white_player_id")
    private User whitePlayer;

    /** ID del jugador de negras (null si es IA) */
    @Column(name = "black_player_id")
    private Long blackPlayerId;

    /**
     * Estado del tablero serializado como JSON.
     * ESTRUCTURA DE DATOS: Representación serializada de la matriz 8×8.
     */
    @Column(name = "board_state", columnDefinition = "TEXT", nullable = false)
    private String boardState;

    /** Estado del juego: ACTIVE, CHECK, CHECKMATE, STALEMATE, DRAW */
    @Column(length = 20)
    private String status = "ACTIVE";

    /** Turno actual: WHITE o BLACK */
    @Column(name = "current_turn", length = 5)
    private String currentTurn = "WHITE";

    /** Dificultad de la IA: EASY, MEDIUM, HARD */
    @Column(length = 10)
    private String difficulty = "MEDIUM";

    /** Ganador: WHITE, BLACK, DRAW, o null */
    @Column(length = 5)
    private String winner;

    /**
     * Lista de movimientos de la partida.
     * ESTRUCTURA DE DATOS: Lista ordenada (historial).
     * Relación OneToMany con la entidad Move.
     */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("moveNumber ASC")
    private List<Move> moves = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // === Constructores ===

    public Game() {}

    public Game(User whitePlayer, String boardState, String difficulty) {
        this.whitePlayer = whitePlayer;
        this.boardState = boardState;
        this.difficulty = difficulty;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // === Métodos de negocio ===

    /** Agrega un movimiento al historial */
    public void addMove(Move move) {
        moves.add(move);
        move.setGame(this);
        this.updatedAt = LocalDateTime.now();
    }

    // === Getters y Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getWhitePlayer() { return whitePlayer; }
    public void setWhitePlayer(User whitePlayer) { this.whitePlayer = whitePlayer; }

    public Long getBlackPlayerId() { return blackPlayerId; }
    public void setBlackPlayerId(Long blackPlayerId) { this.blackPlayerId = blackPlayerId; }

    public String getBoardState() { return boardState; }
    public void setBoardState(String boardState) {
        this.boardState = boardState;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
