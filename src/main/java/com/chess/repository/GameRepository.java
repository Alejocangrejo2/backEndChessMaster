package com.chess.repository;

import com.chess.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * PATRÓN REPOSITORY: Abstrae el acceso a datos de partidas.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /** Busca partidas activas de un jugador (como blancas) */
    List<Game> findByWhitePlayerIdAndStatus(Long playerId, String status);

    /** Busca todas las partidas de un jugador */
    List<Game> findByWhitePlayerIdOrderByCreatedAtDesc(Long playerId);
}
