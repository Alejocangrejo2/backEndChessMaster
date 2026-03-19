package com.chess.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO — Solicitud de movimiento.
 * Contiene la casilla de origen y destino en notación algebraica.
 */
public class MoveRequestDTO {

    @NotBlank(message = "La casilla de origen es requerida")
    private String fromSquare;

    @NotBlank(message = "La casilla de destino es requerida")
    private String toSquare;

    public MoveRequestDTO() {}

    public String getFromSquare() { return fromSquare; }
    public void setFromSquare(String fromSquare) { this.fromSquare = fromSquare; }

    public String getToSquare() { return toSquare; }
    public void setToSquare(String toSquare) { this.toSquare = toSquare; }
}
