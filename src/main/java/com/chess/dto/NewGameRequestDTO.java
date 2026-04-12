package com.chess.dto;

/**
 * DTO — Solicitud de nueva partida.
 * Acepta el nivel de dificultad de la IA.
 */
public class NewGameRequestDTO {

    /** Dificultad: "easy", "medium", o "hard" */
    private String difficulty = "medium";

    public NewGameRequestDTO() {}

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
