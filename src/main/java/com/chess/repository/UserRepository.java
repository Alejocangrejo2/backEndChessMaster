package com.chess.repository;

import com.chess.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * PATRÓN REPOSITORY: Abstrae el acceso a datos de usuarios.
 * Spring Data JPA genera la implementación automáticamente.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Busca un usuario por username */
    Optional<User> findByUsername(String username);

    /** Busca un usuario por email */
    Optional<User> findByEmail(String email);

    /** Verifica si existe un usuario con ese username */
    boolean existsByUsername(String username);

    /** Verifica si existe un usuario con ese email */
    boolean existsByEmail(String email);
}
