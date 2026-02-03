package pl.edu.pwr.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pwr.database.service.GameService;
import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.GameState;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.commands.Command;
import pl.edu.pwr.server.commands.MoveCommand;
import pl.edu.pwr.server.commands.PassCommand;
import pl.edu.pwr.server.commands.SurrenderCommand;
import pl.edu.pwr.server.commands.RemoveCommand;
import pl.edu.pwr.server.commands.FillCommand;
import pl.edu.pwr.server.commands.BotCommand;
import pl.edu.pwr.server.commands.JoinCommand;

import pl.edu.pwr.logic.SimpleBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Obsługuje połączenie i komunikację z jednym klientem gry w Go.
 * <p>
 * Odpowiada za:
 * <ul>
 * <li>Zarządzanie połączeniem sieciowym z klientem (Socket).</li>
 * <li>Parsowanie i wykonywanie komend przesyłanych przez gracza.</li>
 * <li>Wysyłanie stanu gry i komunikatów zwrotnych do klienta.</li>
 * <li>Synchronizację gry między dwoma ludźmi (PvP) lub graczem a botem (PvE).</li>
 * </ul>
 * <p>
 * Klasa implementuje {@link Runnable}, dzięki czemu każda sesja klienta działa w osobnym wątku.
 *
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.1
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    /** Socket do komunikacji sieciowej z klientem. */
    private Socket socket;

    /** Bufor wejściowy do odczytywania komend od klienta. */
    private BufferedReader in;

    /** Strumień wyjściowy do wysyłania wiadomości do klienta. */
    private PrintWriter out;

    /** Identyfikator gracza w danej grze (1 - Czarny, 2 - Biały). */
    private int playerId;

    /** Referencja do obiektu obsługującego drugiego gracza (tylko w trybie PvP). */
    private ClientHandler opponent;

    /** Kolor kamieni przydzielony temu graczowi (BLACK lub WHITE). */
    private Stone myColor;

    /** Instancja gry, w której uczestniczy ten klient. */
    private Game game;

    /** Mapa dostępnych komend (klucz: nazwa komendy, wartość: obiekt wykonawczy). */
    private Map<String, Command> commands = new HashMap<>();

    /** Serwis do obsługi operacji na bazie danych. */
    private GameService gameService;

    /** Unikalne ID gry w bazie danych. */
    private Long gameId;

    /** Flaga określająca, czy gra toczy się przeciwko botowi. */
    private boolean isBotGame = false;

    /** Instancja prostej sztucznej inteligencji (używana, gdy isBotGame = true). */
    private SimpleBot bot;

    /** Referencja do serwera (potrzebna np. do obsługi kolejki JOIN). */
    private Server server;

    /**
     * Tworzy nowy uchwyt klienta.
     * <p>
     * Inicjalizuje podstawowe komendy dostępne przed rozpoczęciem gry (np. JOIN, BOT).
     *
     * @param socket      aktywne połączenie sieciowe z klientem
     * @param gameService serwis bazodanowy
     * @param server      referencja do głównego serwera
     */
    public ClientHandler(Socket socket, GameService gameService, Server server) {
        this.socket = socket;
        this.gameService = gameService;
        this.server = server;

        // Rejestracja komend startowych
        commands.put("BOT", new BotCommand(gameService));
        commands.put("JOIN", new JoinCommand(server));

        // Rejestracja komend gry (wstępnie z nullami, zostaną nadpisane w assignGame)
        // Jest to potrzebne, aby uniknąć NullPointerException przy próbie dostępu przed przypisaniem gry,
        // choć lepszą praktyką jest assignGame. Tutaj inicjalizujemy dla bezpieczeństwa.
        commands.put("MOVE", new MoveCommand(game, gameService, gameId));
        commands.put("PASS", new PassCommand(game, gameService, gameId));
        commands.put("SURRENDER", new SurrenderCommand(game));
        commands.put("REMOVE", new RemoveCommand(game, gameService, gameId));
        commands.put("FILL", new FillCommand(game, gameService, gameId));
    }

    /**
     * Przypisuje klienta do konkretnej rozgrywki.
     * <p>
     * Metoda ustawia referencję do gry, ID gracza, kolor kamieni oraz
     * rejestruje/aktualizuje komendy, które wymagają aktywnej instancji gry (MOVE, PASS itp.).
     *
     * @param game     instancja gry logicznej
     * @param gameId   identyfikator gry w bazie danych
     * @param playerId numer gracza (1 lub 2)
     */
    public void assignGame(Game game, Long gameId, int playerId) {
        this.game = game;
        this.gameId = gameId;
        this.playerId = playerId;
        this.myColor = (playerId == 1) ? Stone.BLACK : Stone.WHITE;

        // Rejestracja komend związanych z aktywną rozgrywką
        commands.put("MOVE", new MoveCommand(game, gameService, gameId));
        commands.put("PASS", new PassCommand(game, gameService, gameId));
        commands.put("SURRENDER", new SurrenderCommand(game));
        commands.put("REMOVE", new RemoveCommand(game, gameService, gameId));
        commands.put("FILL", new FillCommand(game, gameService, gameId));
        commands.put("JOIN", new JoinCommand(server));
    }

    /**
     * Aktywuje tryb gry z botem.
     * Ustawia flagę {@code isBotGame} i tworzy instancję bota.
     */
    public void enableBotMode() {
        this.isBotGame = true;
        this.bot = new SimpleBot();
    }

    /**
     * Zwraca instancję gry przypisaną do tego klienta.
     * @return obiekt gry
     */
    public Game getGame() {
        return game;
    }

    /**
     * Zarządza logiką wykonywania ruchów przez bota.
     * <p>
     * Metoda jest wywoływana po każdej komendzie gracza. Sprawdza stan gry i reaguje w dwóch przypadkach:
     * <ol>
     * <li><b>Gra w toku (IN_PROGRESS):</b> Jeśli tura należy do bota, symuluje czas namysłu, wykonuje ruch i odświeża planszę.</li>
     * <li><b>Faza końcowa (CLEANUP):</b> Jeśli gra jest w fazie usuwania martwych kamieni, bot automatycznie pasuje,
     * pozwalając graczowi na samodzielne oznaczenie terytorium.</li>
     * </ol>
     */
    private void handleBotLogic() {
        // SYTUACJA 1: Normalna gra (stawianie kamieni)
        if (game.getState() == GameState.IN_PROGRESS && game.getCurrentPlayer() != myColor) {

            // 1. Małe opóźnienie dla lepszego wrażenia (UX)
            try {
                Thread.sleep(500); // Pół sekundy przerwy
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            // 2. Bot wykonuje ruch (logika AI)
            boolean moved = bot.performMove(game);

            // 3. Informacja dla gracza co się stało
            if (moved) {
                sendMessage("INFO Bot postawil kamien.");
            } else {
                sendMessage("INFO Bot spasowal.");
            }

            // 4. Aktualizacja widoku planszy u gracza
            sendBoard();
        }

        // SYTUACJA 2: Faza Cleanup (koniec gry, usuwanie martwych kamieni)
        else if (game.getState() == GameState.CLEANUP && game.getCurrentPlayer() != myColor) {
            // Bot ufa graczowi i pasuje, aby nie przeszkadzać w oznaczaniu terenu
            game.pass();

            sendMessage("INFO Bot akceptuje stan planszy (PASS w Cleanup).");

            // Jeśli ten PASS zakończył grę definitywnie -> wyślij wynik
            if (game.isGameOver()) {
                sendBoard();
            }
        }
    }

    /**
     * Ustawia odniesienie do przeciwnika (drugiego gracza).
     * Używane w trybie PvP do przekazywania wiadomości między wątkami.
     * * @param opponent ClientHandler drugiego gracza
     */
    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    /**
     * Zwraca referencję do przeciwnika.
     * * @return ClientHandler drugiego gracza
     */
    public ClientHandler getOpponent() {
        return opponent;
    }

    /**
     * Zwraca gniazdo sieciowe klienta.
     * @return obiekt Socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Zwraca kolor kamieni gracza.
     * * @return Stone reprezentujący kolor (BLACK lub WHITE)
     */
    public Stone getMyColor() {
        return myColor;
    }

    /**
     * Główna pętla wątku obsługującego klienta.
     * <p>
     * Odpowiada za:
     * <ol>
     * <li>Inicjalizację strumieni wejścia/wyjścia.</li>
     * <li>Wysłanie powitania i przydziału koloru.</li>
     * <li>Cykliczne oczekiwanie na komendy tekstowe od klienta.</li>
     * <li>Rozpoznawanie i wykonywanie komend (wzorzec Command).</li>
     * <li>Uruchamianie logiki bota (jeśli dotyczy) po ruchu gracza.</li>
     * <li>Obsługę rozłączenia i zamknięcie zasobów.</li>
     * </ol>
     */
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Jestes graczem: " + (playerId == 1 ? "CZARNYM (B)" : "BIALYM (W)"));

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                logger.debug("Player {} sent command: {}", playerId, inputLine);

                String[] parts = inputLine.split(" ");
                String commandName = parts[0].toUpperCase();

                if(commands.containsKey(commandName)) {
                    // Wykonanie komendy gracza
                    commands.get(commandName).execute(parts, this);

                    // Jeśli gramy z botem i gra trwa, sprawdzamy czy bot ma wykonać ruch
                    if (game != null && isBotGame && !game.isGameOver()) {
                        handleBotLogic();
                    }
                } else {
                    sendMessage("ERR Nieznana komenda. Dostepne: MOVE x y, PASS, SURRENDER");
                }
            }
        } catch (IOException e) {
            logger.info("Player {} disconnected: {}", playerId, e.getMessage());
        } finally {
            try {
                socket.close();
                logger.info("Socket closed for player {}", playerId);
            } catch(IOException e) {
                logger.error("Failed to close socket for player {}", playerId, e);
            }
        }
    }

    /**
     * Generuje i wysyła aktualny stan planszy do klienta.
     * <p>
     * Wiadomość zawiera:
     * <ul>
     * <li>Komendę CLS (czyszczenie ekranu).</li>
     * <li>Tekstową reprezentację planszy (ASCII).</li>
     * <li>Kontekstowe informacje zależne od stanu gry (czyja tura, wynik końcowy, instrukcje fazy Cleanup).</li>
     * </ul>
     */
    public void sendBoard() {
        out.println("CLS");
        out.println(game.getBoard().toString());

        if (game.isGameOver()) {
            out.println("=== GRA ZAKONCZONA ===");
            out.println(game.getGameResult());
        } else if (game.getState() == GameState.CLEANUP) {
            out.println("=== FAZA USUWANIA MARTWYCH KAMIENI ===");
            out.println("Wpisz: REMOVE x y aby usunac kamien przeciwnika");
            out.println("Wpisz: FILL x y aby dodac jencow na teren przeciwnika");
            out.println("Wpisz: PASS gdy usuniesz juz wszystkie martwe kamienie");
        } else {
            if(game.getCurrentPlayer() == myColor) {
                out.println("--- TWOJA TURA (" + (myColor == Stone.BLACK ? "CZARNY" : "BIALY") + ") ---");
            } else {
                out.println("--- TURA PRZECIWNIKA ---");
            }
        }
    }

    /**
     * Wysyła surową wiadomość tekstową do klienta.
     * * @param message treść wiadomości
     */
    public void sendMessage(String message) {
        out.println(message);
    }
}