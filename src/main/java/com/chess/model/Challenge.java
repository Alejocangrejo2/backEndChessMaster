package com.chess.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo in-memory para retos entre jugadores.
 * TTL de 70 segundos — no se persiste en BD.
 */
public class Challenge {

    private String id;
    private String fromUser;
    private String toUser;
    private String roomCode;
    private long timestamp;
    private String status; // pending, accepted, rejected, expired

    public Challenge() {}

    public Challenge(String fromUser, String toUser) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.roomCode = null; // Set by ChallengeService.accept()
        this.timestamp = Instant.now().toEpochMilli();
        this.status = "pending";
    }

    public boolean isExpired() {
        return Instant.now().toEpochMilli() - timestamp > 70_000;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFrom() { return fromUser; }
    public void setFrom(String fromUser) { this.fromUser = fromUser; }

    public String getTo() { return toUser; }
    public void setTo(String toUser) { this.toUser = toUser; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
