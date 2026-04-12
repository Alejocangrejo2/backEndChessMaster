-- ============================================
-- schema.sql — Esquema de la base de datos PostgreSQL
-- ============================================
-- Se ejecuta automáticamente al iniciar el contenedor Docker.
-- Las tablas también se crean vía Hibernate (ddl-auto=update),
-- pero este script sirve como referencia y para Docker init.

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    rating      INTEGER DEFAULT 1200,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de partidas
CREATE TABLE IF NOT EXISTS games (
    id                BIGSERIAL PRIMARY KEY,
    white_player_id   BIGINT REFERENCES users(id),
    black_player_id   BIGINT,
    board_state       TEXT NOT NULL,
    status            VARCHAR(20) DEFAULT 'ACTIVE',
    current_turn      VARCHAR(5) DEFAULT 'WHITE',
    difficulty        VARCHAR(10) DEFAULT 'MEDIUM',
    winner            VARCHAR(5),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de movimientos (historial)
CREATE TABLE IF NOT EXISTS moves (
    id          BIGSERIAL PRIMARY KEY,
    game_id     BIGINT REFERENCES games(id) ON DELETE CASCADE,
    move_number INTEGER NOT NULL,
    from_square VARCHAR(2) NOT NULL,
    to_square   VARCHAR(2) NOT NULL,
    piece_type  VARCHAR(10) NOT NULL,
    piece_color VARCHAR(5) NOT NULL,
    captured    VARCHAR(10),
    notation    VARCHAR(10),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_games_white_player ON games(white_player_id);
CREATE INDEX IF NOT EXISTS idx_games_status ON games(status);
CREATE INDEX IF NOT EXISTS idx_moves_game_id ON moves(game_id);
CREATE INDEX IF NOT EXISTS idx_moves_game_number ON moves(game_id, move_number);
