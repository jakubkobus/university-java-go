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
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Serwer gry w Go obsługujący połączenia graczy.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Nasłuchiwanie przychodzących połączeń od klientów</li>
 *   <li>Parowanie graczy w pary do gry</li>
 *   <li>Tworzenie instancji gry dla każdej pary graczy</li>
 *   <li>Utworzenie wątków ClientHandler dla obsługi komunikacji</li>
 *   <li>Zarządzanie cyklem życia serwera</li>
 * </ul>
 * 
 * Komponent zarządzany przez Springa, który przyjmuje dwóch graczy,
 * tworzy grę o rozmiarze 19x19 i uruchamia ich w osobnych wątkach.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ClientHandler
 * @see Game
 */
@Component
public class Server {
  
  private static final Logger logger = LoggerFactory.getLogger(Server.class);
  
  /** Flaga określająca, czy serwer powinien być uruchomiony */
  private volatile boolean isRunning;

  /** Serwis do zarządzania grami w bazie danych */
  private final GameService gameService;
  
  /** Konfiguracja serwera */
  private final ServerProperties serverProperties;

  /**
   * Konstruktor z wstrzykiwaniem zależności.
   * 
   * @param gameService serwis do zarządzania grami w bazie danych
   * @param serverProperties konfiguracja serwera
   */
  @Autowired
  public Server(GameService gameService, ServerProperties serverProperties) {
    this.gameService = gameService;
    this.serverProperties = serverProperties;
    this.isRunning = serverProperties.isRunning();
  }

  /**
   * Uruchamia serwer na skonfigurowanym porcie.
   * 
   * Przepływ:
   * <ol>
   *   <li>Tworzy ServerSocket na skonfigurowanym porcie</li>
   *   <li>Wciąż nasłuchuje przychodzących połączeń graczy</li>
   *   <li>Akceptuje pierwszego gracza (CZARNY) i oczekuje na drugiego</li>
   *   <li>Akceptuje drugiego gracza (BIAŁY) i zaczyna grę</li>
   *   <li>Tworzy instancję gry o rozmiarze 19x19</li>
   *   <li>Tworzy ClientHandlery dla obu graczy i paruje ich</li>
   *   <li>Uruchamia wątki obsługujące komunikację z oboma graczami</li>
   *   <li>Powraca do nasłuchiwania w celu obsługi następnej pary graczy</li>
   * </ol>
   * 
   * Błędy I/O są wypisywane na standardowe wyjście błędu.
   */
  public void start() {
    int port = serverProperties.getPort();
    logger.info("Server starting on port {}", port);
    
    try(ServerSocket listener = new ServerSocket(port)) {

      while(isRunning) {
        Socket player1 = listener.accept();
        logger.info("Player 1 (BLACK) connected from {}", player1.getRemoteSocketAddress());
        
        PrintWriter tempOut1 = new PrintWriter(player1.getOutputStream(), true);
        tempOut1.println(ServerMessages.SERVER_CONNECTED_PLAYER1);

        Socket player2 = listener.accept();
        logger.info("Player 2 (WHITE) connected from {}", player2.getRemoteSocketAddress());
        PrintWriter tempOut2 = new PrintWriter(player2.getOutputStream(), true);
        tempOut2.println(ServerMessages.SERVER_CONNECTED_PLAYER2);
        
        tempOut1.println(ServerMessages.SERVER_OPPONENT_JOINED);

        Game game = new Game(GameConstants.STANDARD_BOARD_SIZE);

        Long gameId = null;
        if (gameService != null) {
          gameId = gameService.startNewGame("HUMAN", "HUMAN");
          logger.info("Created new game in database with ID: {}", gameId);
        }

        ClientHandler handler1 = new ClientHandler(player1, 1, game, gameService, gameId);
        ClientHandler handler2 = new ClientHandler(player2, 2, game, gameService, gameId);

        handler1.setOpponent(handler2);
        handler2.setOpponent(handler1);

        new Thread(handler1).start();
        new Thread(handler2).start();
        
        logger.info("Game started between player 1 and player 2");
      }

    } catch(IOException e) {
      logger.error("Server error on port {}: {}", port, e.getMessage(), e);
    }
  }

  /**
   * Zatrzymuje serwer.
   */
  public void stop() {
    this.isRunning = false;
  }
  
  /**
   * Sprawdza czy serwer jest uruchomiony.
   * 
   * @return true jeśli serwer jest uruchomiony
   */
  public boolean isRunning() {
    return isRunning;
  }
}