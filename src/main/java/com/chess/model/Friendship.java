package com.chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA — Relación de amistad entre dos usuarios.
 *
 * Estados:
 * - PENDING: solicitud enviada, esperando aceptación
 * - ACCEPTED: amistad activa
 * - REJECTED: solicitud rechazada
 */
@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    // === Constructors ===
    public Friendship() {}

    public Friendship(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = FriendshipStatus.PENDING;
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
