package com.chess.service;

import com.chess.model.Challenge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servicio de retos — almacenamiento in-memory con limpieza automatica.
 * Los retos expiran despues de 70 segundos.
 */
@Service
public class ChallengeService {

    // id -> Challenge
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final GameRoomService roomService;

    public ChallengeService(GameRoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Crear un nuevo reto de fromUser hacia toUser.
     */
    public Challenge sendChallenge(String fromUser, String toUser) {
        // Cancelar retos previos del mismo usuario
        challenges.values().removeIf(c ->
            c.getFrom().equals(fromUser) && c.getStatus().equals("pending")
        );

        Challenge challenge = new Challenge(fromUser, toUser);
        challenges.put(challenge.getId(), challenge);
        return challenge;
    }

    /**
     * Obtener retos pendientes PARA un usuario (los que le enviaron).
     */
    public List<Challenge> getPendingFor(String username) {
        cleanExpired();
        return challenges.values().stream()
            .filter(c -> c.getTo().equalsIgnoreCase(username))
            .filter(c -> "pending".equals(c.getStatus()))
            .filter(c -> !c.isExpired())
            .collect(Collectors.toList());
    }

    /**
     * Obtener retos enviados POR un usuario (para ver si fueron aceptados).
     */
    public List<Challenge> getSentBy(String username) {
        cleanExpired();
        return challenges.values().stream()
            .filter(c -> c.getFrom().equalsIgnoreCase(username))
            .filter(c -> !c.isExpired())
            .collect(Collectors.toList());
    }

    /**
     * Aceptar un reto. Crea la sala automaticamente y une a ambos jugadores.
     */
    public Challenge accept(String challengeId) {
        Challenge c = challenges.get(challengeId);
        if (c == null || c.isExpired()) return null;
        
        // Create room with challenger as white
        String roomCode = roomService.createRoom(c.getFrom());
        // Join the accepter as black
        roomService.joinRoom(roomCode, c.getTo());
        
        c.setRoomCode(roomCode);
        c.setStatus("accepted");
        return c;
    }

    /**
     * Rechazar un reto.
     */
    public Challenge reject(String challengeId) {
        Challenge c = challenges.get(challengeId);
        if (c == null) return null;
        c.setStatus("rejected");
        return c;
    }

    /**
     * Limpiar retos expirados cada 30 segundos.
     */
    @Scheduled(fixedRate = 30000)
    public void cleanExpired() {
        challenges.values().removeIf(c -> c.isExpired() && "pending".equals(c.getStatus()));
    }
}
