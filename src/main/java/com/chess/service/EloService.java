package com.chess.service;

import com.chess.model.User;
import com.chess.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de cálculo de Elo rating.
 *
 * Implementa el sistema estándar de calificación Elo
 * usado en ajedrez profesional (FIDE).
 *
 * Fórmula: newRating = oldRating + K * (resultado - esperado)
 * K = 32 (factor de desarrollo)
 */
@Service
public class EloService {

    private static final int K_FACTOR = 32;
    private static final int INITIAL_RATING = 1200;

    private final UserRepository userRepository;

    public EloService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Calcula el resultado esperado para un jugador.
     *
     * @param playerRating  Rating del jugador
     * @param opponentRating Rating del oponente
     * @return Probabilidad esperada de ganar (0.0 a 1.0)
     */
    public double expectedScore(int playerRating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentRating - playerRating) / 400.0));
    }

    /**
     * Calcula el nuevo rating después de una partida.
     *
     * @param currentRating Rating actual
     * @param expectedScore Resultado esperado (de expectedScore())
     * @param actualScore   Resultado real (1.0=victoria, 0.5=empate, 0.0=derrota)
     * @return Nuevo rating
     */
    public int calculateNewRating(int currentRating, double expectedScore, double actualScore) {
        int newRating = (int) Math.round(currentRating + K_FACTOR * (actualScore - expectedScore));
        return Math.max(100, newRating); // Never go below 100
    }

    /**
     * Actualiza el Elo de ambos jugadores después de una partida.
     *
     * @param winnerUsername Username del ganador (null si empate)
     * @param player1Username Username del jugador 1
     * @param player2Username Username del jugador 2
     * @return Array con los nuevos ratings [player1New, player2New]
     */
    @Transactional
    public int[] updateRatings(String winnerUsername, String player1Username, String player2Username) {
        User player1 = userRepository.findByUsername(player1Username)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + player1Username));
        User player2 = userRepository.findByUsername(player2Username)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + player2Username));

        int r1 = player1.getRating();
        int r2 = player2.getRating();

        double e1 = expectedScore(r1, r2);
        double e2 = expectedScore(r2, r1);

        double s1, s2;
        if (winnerUsername == null) {
            // Draw
            s1 = 0.5;
            s2 = 0.5;
        } else if (winnerUsername.equals(player1Username)) {
            s1 = 1.0;
            s2 = 0.0;
        } else {
            s1 = 0.0;
            s2 = 1.0;
        }

        int newR1 = calculateNewRating(r1, e1, s1);
        int newR2 = calculateNewRating(r2, e2, s2);

        player1.setRating(newR1);
        player2.setRating(newR2);
        userRepository.save(player1);
        userRepository.save(player2);

        return new int[]{newR1, newR2};
    }
}
