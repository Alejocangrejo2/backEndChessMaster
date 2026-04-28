package com.chess.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de salas multijugador en memoria.
 * Flujo: crear sala -> compartir codigo -> unirse -> polling de estado -> movimientos.
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

    public GameRoom makeMove(String code, String username, String from, String to, String newFen) {
        GameRoom room = rooms.get(code.toUpperCase());
        if (room == null) throw new RuntimeException("Sala no encontrada");
        if (!"ACTIVE".equals(room.status)) throw new RuntimeException("Partida no activa");

        boolean isWhiteTurn = "white".equals(room.currentTurn);
        String expectedPlayer = isWhiteTurn ? room.whitePlayer : room.blackPlayer;
        if (!username.equals(expectedPlayer)) throw new RuntimeException("No es tu turno");

        GameMove move = new GameMove();
        move.from = from;
        move.to = to;
        move.player = username;
        move.fen = newFen;
        move.moveNumber = room.moves.size() + 1;
        room.moves.add(move);

        room.fen = newFen;
        room.currentTurn = isWhiteTurn ? "black" : "white";
        room.lastMove = from + "," + to;
        room.lastActivity = Instant.now();
        return room;
    }

    public GameRoom endGame(String code, String status, String winner) {
        GameRoom room = rooms.get(code.toUpperCase());
        if (room == null) throw new RuntimeException("Sala no encontrada");
        room.status = status;
        room.winner = winner;
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

    public static class GameRoom {
        public String code, whitePlayer, blackPlayer, status, fen, currentTurn, lastMove, winner;
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
            map.put("moveCount", moves != null ? moves.size() : 0);
            if (forUsername != null) {
                if (forUsername.equals(whitePlayer)) map.put("myColor", "white");
                else if (forUsername.equals(blackPlayer)) map.put("myColor", "black");
            }
            return map;
        }
    }

    public static class GameMove {
        public int moveNumber;
        public String from, to, player, fen;
    }
}
