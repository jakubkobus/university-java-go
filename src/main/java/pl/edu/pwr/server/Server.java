package pl.edu.pwr.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.pwr.config.ServerProperties;
import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.GameConstants;
import pl.edu.pwr.database.service.GameService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Serwer gry w Go obsługujący połączenia graczy.
 * <p>
 * Odpowiada za:
 * <ul>
 * <li>Nasłuchiwanie przychodzących połączeń od klientów.</li>
 * <li>Parowanie graczy w pary do gry (PvP).</li>
 * <li>Tworzenie instancji gry dla każdej pary graczy.</li>
 * <li>Utworzenie wątków {@link ClientHandler} dla obsługi komunikacji.</li>
 * <li>Zarządzanie cyklem życia serwera.</li>
 * </ul>
 * <p>
 * Komponent zarządzany przez Springa, który przyjmuje graczy, tworzy grę o rozmiarze 19x19
 * i uruchamia ich obsługę w osobnych wątkach.
 * * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ClientHandler
 * @see Game
 */
@Component
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /** Flaga określająca, czy serwer powinien być uruchomiony. */
    private volatile boolean isRunning;

    /** Serwis do zarządzania grami w bazie danych. */
    private final GameService gameService;

    /** Konfiguracja serwera (port, ustawienia). */
    private final ServerProperties serverProperties;

    /** Referencja do gracza oczekującego na przeciwnika w trybie PvP. */
    private ClientHandler waitingPlayer = null;

    /**
     * Konstruktor z wstrzykiwaniem zależności przez Springa.
     * * @param gameService serwis do zarządzania grami w bazie danych
     * @param serverProperties obiekt konfiguracyjny serwera
     */
    @Autowired
    public Server(GameService gameService, ServerProperties serverProperties) {
        this.gameService = gameService;
        this.serverProperties = serverProperties;
        this.isRunning = serverProperties.isRunning();
    }

    /**
     * Uruchamia główną pętlę serwera na skonfigurowanym porcie.
     * <p>
     * Przepływ działania:
     * <ol>
     * <li>Tworzy gniazdo serwera (ServerSocket).</li>
     * <li>W pętli nieskończonej akceptuje nowe połączenia przychodzące (Socket).</li>
     * <li>Dla każdego połączenia tworzy obiekt {@link ClientHandler} (początkowo bez przypisanej gry).</li>
     * <li>Przekazuje referencję do serwera ('this'), aby handler mógł korzystać z kolejki PvP.</li>
     * <li>Uruchamia wątek obsługi klienta, gdzie następuje wybór trybu gry (BOT/JOIN).</li>
     * </ol>
     * W przypadku błędów wejścia/wyjścia loguje odpowiednie komunikaty.
     */
    public void start() {
        int port = serverProperties.getPort();
        logger.info("Server starting on port {}", port);

        try(ServerSocket listener = new ServerSocket(port)) {

            while(isRunning) {
                Socket socket = listener.accept();
                logger.info("New connection from {}", socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket, gameService, this);

                new Thread(handler).start();
            }

        } catch(IOException e) {
            logger.error("Server error on port {}: {}", port, e.getMessage(), e);
        }
    }

    /**
     * Obsługuje logikę parowania graczy w trybie PvP (Player vs Player).
     * Metoda jest synchronizowana, aby uniknąć problemów przy jednoczesnym dostępie wielu wątków.
     * <p>
     * Algorytm działania:
     * <ol>
     * <li>Sprawdza, czy w polu {@code waitingPlayer} znajduje się aktywny gracz oczekujący.</li>
     * <li><b>Jeśli tak (Mamy parę):</b>
     * <ul>
     * <li>Pobiera oczekującego gracza jako Gracza 1 (Czarnego).</li>
     * <li>Traktuje nowego gracza jako Gracza 2 (Białego).</li>
     * <li>Tworzy nową instancję gry i zapisuje ją w bazie danych (HUMAN vs HUMAN).</li>
     * <li>Przypisuje grę obu handlerom i łączy ich jako przeciwników.</li>
     * <li>Wysyła komunikaty startowe i czyści kolejkę oczekujących.</li>
     * </ul>
     * </li>
     * <li><b>Jeśli nie (Kolejka pusta):</b>
     * <ul>
     * <li>Ustawia bieżącego gracza jako {@code waitingPlayer}.</li>
     * <li>Wysyła komunikat o oczekiwaniu na przeciwnika.</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param newPlayer handler gracza, który wysłał komendę JOIN
     */
    public synchronized void joinPvPQueue(ClientHandler newPlayer) {
        if (waitingPlayer != null && waitingPlayer.getSocket().isConnected() && !waitingPlayer.getSocket().isClosed()) {

            logger.info("Pairing players for PvP...");

            ClientHandler player1 = waitingPlayer;
            ClientHandler player2 = newPlayer;

            waitingPlayer = null;

            Game game = new Game(GameConstants.STANDARD_BOARD_SIZE);
            Long gameId = (gameService != null) ? gameService.startNewGame("HUMAN", "HUMAN") : null;

            player1.assignGame(game, gameId, 1);
            player2.assignGame(game, gameId, 2);

            player1.setOpponent(player2);
            player2.setOpponent(player1);

            player1.sendMessage(ServerMessages.SERVER_CONNECTED_PLAYER1);
            player1.sendMessage(ServerMessages.SERVER_OPPONENT_JOINED);
            player1.sendBoard();

            player2.sendMessage(ServerMessages.SERVER_CONNECTED_PLAYER2);
            player2.sendBoard();

        } else {
            logger.info("Player added to waiting queue");
            this.waitingPlayer = newPlayer;
            newPlayer.sendMessage("[SERWER] Oczekiwanie na drugiego gracza...");
        }
    }

    /**
     * Zatrzymuje pętlę serwera.
     * Ustawia flagę {@code isRunning} na false.
     */
    public void stop() {
        this.isRunning = false;
    }

    /**
     * Sprawdza status działania serwera.
     * * @return true, jeśli serwer jest uruchomiony i akceptuje połączenia.
     */
    public boolean isRunning() {
        return isRunning;
    }
}