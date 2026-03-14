package com.chess.dto;

/**
 * DTO — Respuesta de autenticación con JWT token.
 */
public class AuthResponseDTO {

    private String token;
    private String username;
    private Integer rating;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, String username, Integer rating) {
        this.token = token;
        this.username = username;
        this.rating = rating;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
