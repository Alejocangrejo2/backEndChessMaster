package com.chess.repository;

import com.chess.model.Friendship;
import com.chess.model.Friendship.FriendshipStatus;
import com.chess.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** Solicitudes pendientes recibidas por un usuario */
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

    /** Todas las amistades aceptadas de un usuario (como sender o receiver) */
    @Query("SELECT f FROM Friendship f WHERE (f.sender = :user OR f.receiver = :user) AND f.status = :status")
    List<Friendship> findAllByUserAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status);

    /** Verifica si ya existe una relación entre dos usuarios */
    @Query("SELECT f FROM Friendship f WHERE (f.sender = :u1 AND f.receiver = :u2) OR (f.sender = :u2 AND f.receiver = :u1)")
    Optional<Friendship> findBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);
}
