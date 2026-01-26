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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Obsługuje połączenie i komunikację z jednym klientem gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Zarządzanie połączeniem sieciowym z klientem</li>
 *   <li>Odczytywanie i przetwarzanie komend od klienta</li>
 *   <li>Wysyłanie stanu gry i wiadomości do klienta</li>
 *   <li>Egzekwowanie komend na obiekcie gry</li>
 *   <li>Synchronizacja akcji między dwoma graczami</li>
 * </ul>
 * 
 * Każdy ClientHandler działa w osobnym wątku i obsługuje jednego gracza.
 * ClientHandlery są sparowane (opponent) do komunikacji między graczami.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Server
 * @see Command
 */
public class ClientHandler implements Runnable {
  
  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
  
  /** Socket do komunikacji z klientem */
  private Socket socket;
  
  /** BufferedReader do odczytywania komend od klienta */
  private BufferedReader in;
  
  /** PrintWriter do wysyłania wiadomości do klienta */
  private PrintWriter out;
  
  /** Identyfikator gracza (1 lub 2) */
  private int playerId;
  
  /** Referencja do przeciwnika (drugi gracz) */
  private ClientHandler opponent;
  
  /** Kolor kamieni przydzielony temu graczowi (BLACK lub WHITE) */
  private Stone myColor;
  
  /** Instancja gry, na której gracze grają */
  private Game game;

  /** Mapa dostępnych komend */
  private Map<String, Command> commands = new HashMap<>();

  /** Serwis do zarządzania grami w bazie danych */
  private GameService gameService;

  /** Identyfikator gry w bazie danych */
  private Long gameId;

  /**
   * Konstruktor ClientHandler.
   * Inicjalizuje obsługę klienta, przydziela kolor kamieni i rejestruje dostępne komendy.
   * 
   * @param socket socket do komunikacji z klientem
   * @param playerId identyfikator gracza (1 = czarny, 2 = biały)
   * @param game instancja gry, w której będzie grać klient
   * @param gameService serwis do zarządzania grami w bazie danych
   * @param gameId identyfikator gry w bazie danych
   */
  public ClientHandler(Socket socket, int playerId, Game game, GameService gameService, Long gameId) {
    this.socket = socket;
    this.playerId = playerId;
    this.game = game;
    this.myColor = (playerId == 1) ? Stone.BLACK : Stone.WHITE;
    this.gameService = gameService;
    this.gameId = gameId;

    commands.put("MOVE", new MoveCommand(game, gameService, gameId));
    commands.put("PASS", new PassCommand(game, gameService, gameId));

    commands.put("SURRENDER", new SurrenderCommand(game));
    commands.put("REMOVE", new RemoveCommand(game, gameService, gameId));
    commands.put("FILL", new FillCommand(game, gameService, gameId));
  }

  /**
   * Ustawia odniesienie do przeciwnika (drugiego gracza).
   * 
   * @param opponent ClientHandler drugiego gracza
   */
  public void setOpponent(ClientHandler opponent) {
    this.opponent = opponent;
  }

  /**
   * Zwraca referencję do przeciwnika.
   * 
   * @return ClientHandler drugiego gracza
   */
  public ClientHandler getOpponent() {
    return opponent;
  }

  /**
   * Zwraca kolor kamieni gracza.
   * 
   * @return Stone reprezentujący kolor (BLACK lub WHITE)
   */
  public Stone getMyColor() {
    return myColor;
  }

  /**
   * Główna pętla wątku obsługującego klienta.
   * 
   * Przepływ:
   * <ol>
   *   <li>Inicjalizuje strumienie wejścia/wyjściaprocess</li>
   *   <li>Wysyła początkowy stan planszy i informację o kolorze gracza</li>
   *   <li>Wciąż czeka na i przetwarza komendy od klienta</li>
   *   <li>Parsuje komendę i wyłania jej z mapy dostępnych komend</li>
   *   <li>Wysyła komunikat błędu dla nieznanych komend</li>
   *   <li>Obsługuje połączenia i błędy I/O</li>
   * </ol>
   */
  @Override
  public void run() {
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      sendBoard();
      out.println("Jestes graczem: " + (playerId == 1 ? "CZARNYM (B)" : "BIALYM (W)"));

      String inputLine;
      while((inputLine = in.readLine()) != null) {
        logger.debug("Player {} sent command: {}", playerId, inputLine);

        String[] parts = inputLine.split(" ");
        String commandName = parts[0].toUpperCase();

        if(commands.containsKey(commandName)) {
          commands.get(commandName).execute(parts, this);
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
   * Wysyła aktualny stan planszy do klienta.
   * 
   * Wysyła:
   * <ul>
   *   <li>Komendę czyszczenia ekranu (CLS)</li>
   *   <li>Wizualizację planszy</li>
   *   <li>Informacje o stanie gry (czyja tura, czy gra skończona, itp.)</li>
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
   * Wysyła wiadomość tekstową do klienta.
   * 
   * @param message treść wiadomości do wysłania
   */
  public void sendMessage(String message) {
    out.println(message);
  }
}