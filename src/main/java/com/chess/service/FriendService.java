package com.chess.service;

import com.chess.model.Friendship;
import com.chess.model.Friendship.FriendshipStatus;
import com.chess.model.User;
import com.chess.repository.FriendshipRepository;
import com.chess.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de amistades.
 *
 * Funcionalidades:
 * - Buscar usuarios por nombre
 * - Enviar / aceptar / rechazar solicitudes de amistad
 * - Lista de amigos con estado online
 * - Eliminar amigos
 */
@Service
public class FriendService {

    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;

    // Track online users (in-memory, replaced by WebSocket later)
    private final Set<String> onlineUsers = Collections.synchronizedSet(new HashSet<>());

    public FriendService(FriendshipRepository friendshipRepo, UserRepository userRepo) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
    }

    /**
     * Search users by username (partial match).
     */
    public List<Map<String, Object>> searchUsers(String query, String currentUsername) {
        if (query == null || query.length() < 2) return List.of();

        // Find users whose username contains the query (case insensitive)
        return userRepo.findAll().stream()
            .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()))
            .filter(u -> !u.getUsername().equals(currentUsername))
            .limit(10)
            .map(u -> {
                Map<String, Object> result = new HashMap<>();
                result.put("username", u.getUsername());
                result.put("rating", u.getRating());
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * Send a friend request.
     */
    @Transactional
    public Friendship sendRequest(String fromUsername, String toUsername) {
        if (fromUsername.equals(toUsername)) {
            throw new IllegalArgumentException("No puedes agregarte a ti mismo");
        }

        User sender = userRepo.findByUsername(fromUsername)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + fromUsername));
        User receiver = userRepo.findByUsername(toUsername)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + toUsername));

        // Check for existing relationship
        Optional<Friendship> existing = friendshipRepo.findBetweenUsers(sender, receiver);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalArgumentException("Ya son amigos");
            }
            if (f.getStatus() == FriendshipStatus.PENDING) {
                throw new IllegalArgumentException("Ya hay una solicitud pendiente");
            }
            // If rejected, allow new request
            f.setStatus(FriendshipStatus.PENDING);
            f.setSender(sender);
            f.setReceiver(receiver);
            return friendshipRepo.save(f);
        }

        return friendshipRepo.save(new Friendship(sender, receiver));
    }

    /**
     * Accept a friend request.
     */
    @Transactional
    public void acceptRequest(Long requestId, String currentUsername) {
        Friendship f = friendshipRepo.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!f.getReceiver().getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Solo el destinatario puede aceptar");
        }

        f.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepo.save(f);
    }

    /**
     * Reject a friend request.
     */
    @Transactional
    public void rejectRequest(Long requestId, String currentUsername) {
        Friendship f = friendshipRepo.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!f.getReceiver().getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Solo el destinatario puede rechazar");
        }

        f.setStatus(FriendshipStatus.REJECTED);
        friendshipRepo.save(f);
    }

    /**
     * Get all accepted friends for a user.
     */
    public List<Map<String, Object>> getFriendsList(String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Friendship> friendships = friendshipRepo.findAllByUserAndStatus(user, FriendshipStatus.ACCEPTED);

        return friendships.stream().map(f -> {
            User friend = f.getSender().getUsername().equals(username) ? f.getReceiver() : f.getSender();
            Map<String, Object> result = new HashMap<>();
            result.put("id", f.getId().toString());
            result.put("username", friend.getUsername());
            result.put("rating", friend.getRating());
            result.put("online", onlineUsers.contains(friend.getUsername()));
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * Get pending friend requests for a user.
     */
    public List<Map<String, Object>> getPendingRequests(String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return friendshipRepo.findByReceiverAndStatus(user, FriendshipStatus.PENDING).stream()
            .map(f -> {
                Map<String, Object> result = new HashMap<>();
                result.put("id", f.getId().toString());
                result.put("fromUser", f.getSender().getUsername());
                result.put("fromRating", f.getSender().getRating());
                result.put("timestamp", f.getCreatedAt().toString());
                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * Remove a friend (delete the friendship).
     */
    @Transactional
    public void removeFriend(Long friendshipId, String currentUsername) {
        Friendship f = friendshipRepo.findById(friendshipId)
            .orElseThrow(() -> new RuntimeException("Amistad no encontrada"));

        boolean isMember = f.getSender().getUsername().equals(currentUsername)
                        || f.getReceiver().getUsername().equals(currentUsername);

        if (!isMember) {
            throw new IllegalArgumentException("No tienes permiso para eliminar esta amistad");
        }

        friendshipRepo.delete(f);
    }

    // === Online tracking (in-memory, future: WebSocket) ===

    public void setOnline(String username) {
        onlineUsers.add(username);
    }

    public void setOffline(String username) {
        onlineUsers.remove(username);
    }
}
