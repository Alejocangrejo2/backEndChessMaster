package com.chess.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servicio de salas multijugador en memoria.
 * Gestiona: crear, unirse, mover, finalizar, historial.
 */
@Service
public class GameRoomService {

    private final ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public String createRoom(String creatorUsername) {
        String code = generateCode();
        GameRoom room = new GameRoom();
        room.code = code;
        room.whitePlayer = creatorUsername;
        room.blackPlayer = null;
        room.status = "WAITING";
        room.fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        room.moves = new ArrayList<>();
        room.currentTurn = "white";
        room.endReason = null;
        room.createdAt = Instant.now();
        room.lastActivity = Instant.now();
        rooms.put(code, room);
        return code;
    }

    public GameRoom joinRoom(String code, String username) {
        GameRoom room = rooms.get(code.toUpperCase());
        if (room == null) throw new RuntimeException("Sala no encontrada: " + code);
        if (!"WAITING".equals(room.status)) throw new RuntimeException("Sala no disponible");
        if (room.whitePlayer.equals(username)) throw new RuntimeException("No puedes unirte a tu propia sala");
        room.blackPlayer = username;
        room.status = "ACTIVE";
        room.lastActivity = Instant.now();
        return room;
    }

    public GameRoom getRoom(String code) {
        GameRoom room = rooms.get(code.toUpperCase());
        if (room == null) throw new RuntimeException("Sala no encontrada");
        return room;
    }

    public GameRoom makeMove(String code, String username, String from, String to, String newFen, String san) {
        GameRoom room = rooms.get(code.toUpperCase());
        if (room == null) throw new RuntimeException("Sala no encontrada");
        if (!"ACTIVE".equals(room.status)) throw new RuntimeException("Partida no activa");

        boolean isWhiteTurn = "white".equals(room.currentTurn);
        String expectedPlayer = isWhiteTurn ? room.whitePlayer : room.blackPlayer;
        if (!username.equals(expectedPlayer)) throw new RuntimeException("No es tu turno");

        GameMove move = new GameMove();
        move.from = from;
        move.to = to;
        move.san = san;
        move.player = username;
        move.color = isWhiteTurn ? "white" : "black";
        move.fen = newFen;
        move.moveNumber = room.moves.size() + 1;
        room.moves.add(move);

        room.fen = newFen;
        room.currentTurn = isWhiteTurn ? "black" : "white";
        room.lastMove = from + "," + to;
        room.lastActivity = Instant.now();
        return room;
    }

    /**
     * Endpoint correcto para terminar partida.
     * Distingue: CHECKMATE, STALEMATE, DRAW, RESIGNED, ABANDONED
     */
    public GameRoom endGame(String code, String endReason, String winner) {
        GameRoom room = rooms.get(code.toUpperCase());
        if (room == null) throw new RuntimeException("Sala no encontrada");
        if ("FINISHED".equals(room.status)) return room; // ya terminada
        room.status = "FINISHED";
        room.endReason = endReason; // CHECKMATE, STALEMATE, DRAW, RESIGNED, ABANDONED
        room.winner = winner;       // "white", "black", or null (draw)
        room.lastActivity = Instant.now();
        return room;
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        String code = sb.toString();
        if (rooms.containsKey(code)) return generateCode();
        return code;
    }

    // === Inner classes ===

    public static class GameRoom {
        public String code, whitePlayer, blackPlayer, status, fen, currentTurn, lastMove, winner, endReason;
        public List<GameMove> moves;
        public Instant createdAt, lastActivity;

        public Map<String, Object> toMap(String forUsername) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("code", code);
            map.put("whitePlayer", whitePlayer);
            map.put("blackPlayer", blackPlayer);
            map.put("status", status);
            map.put("fen", fen);
            map.put("currentTurn", currentTurn);
            map.put("lastMove", lastMove);
            map.put("winner", winner);
            map.put("endReason", endReason);
            map.put("moveCount", moves != null ? moves.size() : 0);
            if (forUsername != null) {
                if (forUsername.equals(whitePlayer)) map.put("myColor", "white");
                else if (forUsername.equals(blackPlayer)) map.put("myColor", "black");
            }
            // Include move list for review
            if (moves != null) {
                map.put("moves", moves.stream().map(m -> {
                    Map<String, Object> mv = new LinkedHashMap<>();
                    mv.put("moveNumber", m.moveNumber);
                    mv.put("from", m.from);
                    mv.put("to", m.to);
                    mv.put("san", m.san);
                    mv.put("color", m.color);
                    mv.put("fen", m.fen);
                    return mv;
                }).collect(Collectors.toList()));
            }
            return map;
        }
    }

    public static class GameMove {
        public int moveNumber;
        public String from, to, san, player, color, fen;
    }
}
