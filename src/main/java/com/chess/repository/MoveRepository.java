package com.chess.repository;

import com.chess.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * PATRÓN REPOSITORY: Abstrae el acceso a datos de movimientos.
 */
@Repository
public interface MoveRepository extends JpaRepository<Move, Long> {

    /** Obtiene todos los movimientos de una partida ordenados */
    List<Move> findByGameIdOrderByMoveNumberAsc(Long gameId);

    /** Cuenta los movimientos de una partida */
    long countByGameId(Long gameId);
}
