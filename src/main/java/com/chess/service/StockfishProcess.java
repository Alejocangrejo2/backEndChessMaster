package com.chess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper para el proceso nativo de Stockfish.
 * Comunica via stdin/stdout con el binario UCI.
 *
 * CRITICAL: Todas las evaluaciones se normalizan a la perspectiva de BLANCAS.
 * Stockfish devuelve el score desde la perspectiva del lado que le toca mover.
 * Si es turno de negras, el score se niega para mantener consistencia.
 */
@Component
public class StockfishProcess {

    private static final Logger log = LoggerFactory.getLogger(StockfishProcess.class);
    private static final Pattern SCORE_CP_PATTERN = Pattern.compile("score cp (-?\\d+)");
    private static final Pattern SCORE_MATE_PATTERN = Pattern.compile("score mate (-?\\d+)");
    private static final Pattern BESTMOVE_PATTERN = Pattern.compile("bestmove (\\S+)");

    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean initialized = false;

    /**
     * Path to Stockfish binary.
     * On deployment: set STOCKFISH_PATH env variable.
     * Default: tries common locations.
     */
    private String getStockfishPath() {
        String envPath = System.getenv("STOCKFISH_PATH");
        if (envPath != null && !envPath.isEmpty()) return envPath;

        // Try common locations
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows: check project directory first
            File local = new File("stockfish/stockfish-windows-x86-64-avx2.exe");
            if (local.exists()) return local.getAbsolutePath();
            local = new File("stockfish/stockfish.exe");
            if (local.exists()) return local.getAbsolutePath();
            return "stockfish.exe"; // Try PATH
        } else {
            // Linux/Mac
            File local = new File("stockfish/stockfish");
            if (local.exists()) return local.getAbsolutePath();
            return "stockfish"; // Try PATH
        }
    }

    @PostConstruct
    public void init() {
        try {
            String path = getStockfishPath();
            log.info("Initializing Stockfish from: {}", path);

            ProcessBuilder pb = new ProcessBuilder(path);
            pb.redirectErrorStream(true);
            process = pb.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            // Initialize UCI protocol
            sendCommand("uci");
            String line;
            while ((line = readLine()) != null) {
                if (line.equals("uciok")) break;
            }

            // Configure for analysis (max strength)
            sendCommand("setoption name Threads value 1");
            sendCommand("setoption name Hash value 128");
            sendCommand("isready");

            while ((line = readLine()) != null) {
                if (line.equals("readyok")) break;
            }

            initialized = true;
            log.info("Stockfish initialized successfully");

        } catch (Exception e) {
            log.warn("Could not initialize Stockfish: {}. Analysis will use fallback.", e.getMessage());
            initialized = false;
        }
    }

    @PreDestroy
    public void destroy() {
        if (process != null) {
            try {
                sendCommand("quit");
                process.waitFor(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
            process.destroyForcibly();
        }
    }

    public boolean isAvailable() {
        return initialized && process != null && process.isAlive();
    }

    /**
     * Evaluate a position and return centipawn score from WHITE's perspective.
     *
     * CRITICAL: Normalizes Stockfish output to always be from white's POV.
     * Stockfish returns score from the side-to-move's perspective.
     *
     * @param fen FEN string of the position to evaluate
     * @param depthLimit Maximum search depth
     * @param timeLimitMs Maximum time in milliseconds
     * @return Evaluation in centipawns from white's perspective
     */
    public synchronized EvalResult evaluate(String fen, int depthLimit, int timeLimitMs) {
        if (!isAvailable()) {
            return new EvalResult(0, "");
        }

        try {
            // Determine whose turn from FEN
            String[] fenParts = fen.split(" ");
            boolean isBlackToMove = fenParts.length > 1 && fenParts[1].equals("b");

            // Stop any previous search, set position
            sendCommand("stop");
            sendCommand("isready");
            waitForReady();

            sendCommand("position fen " + fen);
            sendCommand("go depth " + depthLimit + " movetime " + timeLimitMs);

            int lastScore = 0;
            String bestMove = "";
            String line;

            long startTime = System.currentTimeMillis();
            long maxWait = timeLimitMs + 3000; // Extra buffer

            while ((line = readLine()) != null) {
                if (System.currentTimeMillis() - startTime > maxWait) {
                    sendCommand("stop");
                    break;
                }

                if (line.contains("score cp ")) {
                    Matcher m = SCORE_CP_PATTERN.matcher(line);
                    if (m.find()) {
                        lastScore = Integer.parseInt(m.group(1));
                    }
                } else if (line.contains("score mate ")) {
                    Matcher m = SCORE_MATE_PATTERN.matcher(line);
                    if (m.find()) {
                        int mateIn = Integer.parseInt(m.group(1));
                        lastScore = mateIn > 0 ? 10000 - mateIn * 10 : -10000 + Math.abs(mateIn) * 10;
                    }
                }

                Matcher bm = BESTMOVE_PATTERN.matcher(line);
                if (bm.find()) {
                    bestMove = bm.group(1);
                    break;
                }
            }

            // CRITICAL: Normalize to white's perspective
            int normalizedScore = isBlackToMove ? -lastScore : lastScore;

            return new EvalResult(normalizedScore, bestMove);

        } catch (Exception e) {
            log.error("Stockfish evaluation error: {}", e.getMessage());
            return new EvalResult(0, "");
        }
    }

    private void sendCommand(String cmd) throws IOException {
        if (writer != null) {
            writer.write(cmd);
            writer.newLine();
            writer.flush();
        }
    }

    private String readLine() throws IOException {
        if (reader != null && reader.ready()) {
            return reader.readLine();
        }
        // Wait briefly for output
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        if (reader != null && reader.ready()) {
            return reader.readLine();
        }
        return null;
    }

    private void waitForReady() throws IOException {
        String line;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 3000) {
            line = readLine();
            if (line != null && line.equals("readyok")) return;
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Result of a Stockfish evaluation.
     */
    public static class EvalResult {
        private final int score;     // Centipawns from WHITE's perspective
        private final String bestMove; // UCI format (e.g. "e2e4")

        public EvalResult(int score, String bestMove) {
            this.score = score;
            this.bestMove = bestMove;
        }

        public int getScore() { return score; }
        public String getBestMove() { return bestMove; }
    }
}
