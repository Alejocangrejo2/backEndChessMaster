package com.chess.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO — Solicitud de registro de usuario.
 * Valida que los campos no estén vacíos y que el email sea válido.
 */
public class RegisterRequestDTO {

    @NotBlank(message = "El username es requerido")
    private String username;

    @NotBlank(message = "El correo electronico es requerido")
    @Email(message = "Ingresa un correo electronico valido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    public RegisterRequestDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
