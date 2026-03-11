package com.chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA — Usuario del sistema.
 * PATRÓN REPOSITORY: Se accede a través de UserRepository.
 * 
 * Relaciones:
 * - Un usuario puede tener muchas partidas como blancas
 * - Un usuario puede tener muchas partidas como negras
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /** Contraseña hasheada con BCrypt */
    @Column(nullable = false)
    private String password;

    /** Rating ELO del jugador (default 1200) */
    @Column
    private Integer rating = 1200;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // === Constructores ===

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // === Getters y Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
