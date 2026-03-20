package com.chess.service;

import com.chess.dto.GameStateDTO;
import com.chess.dto.MoveRequestDTO;
import com.chess.dto.NewGameRequestDTO;
import com.chess.model.Game;
import com.chess.model.Move;
import com.chess.model.User;
import com.chess.repository.GameRepository;
import com.chess.repository.MoveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de juego — lógica de negocio para partidas de ajedrez.
 * 
 * PATRÓN MVC: Service layer para la gestión de partidas.
 * Responsabilidades:
 * - Crear nuevas partidas
 * - Registrar movimientos
 * - Retornar estado del juego
 */
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;

    /** Estado inicial del tablero en formato JSON simplificado */
    private static final String INITIAL_BOARD =
        "[[\"br\",\"bn\",\"bb\",\"bq\",\"bk\",\"bb\",\"bn\",\"br\"]," +
        "[\"bp\",\"bp\",\"bp\",\"bp\",\"bp\",\"bp\",\"bp\",\"bp\"]," +
        "[null,null,null,null,null,null,null,null]," +
        "[null,null,null,null,null,null,null,null]," +
        "[null,null,null,null,null,null,null,null]," +
        "[null,null,null,null,null,null,null,null]," +
        "[\"wp\",\"wp\",\"wp\",\"wp\",\"wp\",\"wp\",\"wp\",\"wp\"]," +
        "[\"wr\",\"wn\",\"wb\",\"wq\",\"wk\",\"wb\",\"wn\",\"wr\"]]";

    public GameService(GameRepository gameRepository, MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
    }

    /**
     * Crea una nueva partida.
     * POST /api/game/new
     * 
     * @param request - Solicitud con dificultad de IA
     * @param player - Usuario que inicia la partida
     * @return Estado inicial del juego
     */
    @Transactional
    public GameStateDTO createGame(NewGameRequestDTO request, User player) {
        Game game = new Game(player, INITIAL_BOARD, request.getDifficulty().toUpperCase());
        game = gameRepository.save(game);

        return toGameStateDTO(game);
    }

    /**
     * Registra un movimiento en una partida.
     * POST /api/game/{id}/move
     * 
     * @param gameId - ID de la partida
     * @param request - Movimiento (from, to)
     * @return Estado actualizado del juego
     */
    @Transactional
    public GameStateDTO makeMove(Long gameId, MoveRequestDTO request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada: " + gameId));

        if (!"ACTIVE".equals(game.getStatus()) && !"CHECK".equals(game.getStatus())) {
            throw new RuntimeException("La partida ya ha terminado");
        }

        // Determinar número de movimiento
        int moveNumber = game.getMoves().size() + 1;

        // Determinar color de la pieza (basado en el turno)
        String pieceColor = game.getCurrentTurn();

        // Crear y guardar el movimiento
        Move move = new Move(
            game,
            moveNumber,
            request.getFromSquare(),
            request.getToSquare(),
            "PIECE",  // El frontend maneja la validación completa
            pieceColor,
            null,
            request.getFromSquare() + "-" + request.getToSquare()
        );

        game.addMove(move);

        // Cambiar turno
        game.setCurrentTurn("WHITE".equals(game.getCurrentTurn()) ? "BLACK" : "WHITE");

        gameRepository.save(game);

        return toGameStateDTO(game);
    }

    /**
     * Obtiene el estado actual de una partida.
     * GET /api/game/{id}/state
     * 
     * @param gameId - ID de la partida
     * @return Estado completo del juego
     */
    public GameStateDTO getGameState(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada: " + gameId));

        return toGameStateDTO(game);
    }

    /**
     * Obtiene el historial de partidas de un usuario.
     */
    public List<GameStateDTO> getPlayerGames(Long playerId) {
        return gameRepository.findByWhitePlayerIdOrderByCreatedAtDesc(playerId)
                .stream()
                .map(this::toGameStateDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Game a DTO.
     * Transformación de la capa de dominio a la capa de presentación.
     */
    private GameStateDTO toGameStateDTO(Game game) {
        GameStateDTO dto = new GameStateDTO();
        dto.setGameId(game.getId());
        dto.setBoardState(game.getBoardState());
        dto.setStatus(game.getStatus());
        dto.setCurrentTurn(game.getCurrentTurn());
        dto.setDifficulty(game.getDifficulty());
        dto.setWinner(game.getWinner());

        // Convertir movimientos a DTOs
        List<GameStateDTO.MoveDTO> moveDTOs = game.getMoves().stream()
                .map(m -> new GameStateDTO.MoveDTO(
                        m.getMoveNumber(),
                        m.getFromSquare(),
                        m.getToSquare(),
                        m.getPieceType(),
                        m.getPieceColor(),
                        m.getCaptured(),
                        m.getNotation()
                ))
                .collect(Collectors.toList());

        dto.setMoves(moveDTOs);

        return dto;
    }
}
