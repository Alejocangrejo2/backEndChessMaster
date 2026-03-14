package com.chess.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO — Solicitud de login.
 */
public class LoginRequestDTO {

    @NotBlank(message = "El username es requerido")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    public LoginRequestDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
