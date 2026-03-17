package com.chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA — Movimiento individual de una partida.
 * 
 * Registra el movimiento de una pieza: origen, destino,
 * tipo de pieza, color, captura, y notación algebraica.
 * 
 * ESTRUCTURA DE DATOS: Cada registro es un elemento del
 * historial (Stack) de la partida.
 */
@Entity
@Table(name = "moves")
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Partida a la que pertenece este movimiento */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /** Número de movimiento (1, 2, 3, ...) */
    @Column(name = "move_number", nullable = false)
    private Integer moveNumber;

    /** Casilla de origen (e.g., "e2") */
    @Column(name = "from_square", length = 2, nullable = false)
    private String fromSquare;

    /** Casilla de destino (e.g., "e4") */
    @Column(name = "to_square", length = 2, nullable = false)
    private String toSquare;

    /** Tipo de pieza movida (PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING) */
    @Column(name = "piece_type", length = 10, nullable = false)
    private String pieceType;

    /** Color de la pieza (WHITE, BLACK) */
    @Column(name = "piece_color", length = 5, nullable = false)
    private String pieceColor;

    /** Tipo de pieza capturada (null si no hubo captura) */
    @Column(length = 10)
    private String captured;

    /** Notación algebraica del movimiento (e.g., "e4", "Nf3", "O-O") */
    @Column(length = 10)
    private String notation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // === Constructores ===

    public Move() {}

    public Move(Game game, Integer moveNumber, String fromSquare, String toSquare,
                String pieceType, String pieceColor, String captured, String notation) {
        this.game = game;
        this.moveNumber = moveNumber;
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
        this.captured = captured;
        this.notation = notation;
        this.createdAt = LocalDateTime.now();
    }

    // === Getters y Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
}
