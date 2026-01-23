package pl.edu.pwr.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.pwr.config.ServerProperties;
import pl.edu.pwr.logic.Game;
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
    System.out.println("Serwer uruchamia sie na porcie " + port);
    
    try(ServerSocket listener = new ServerSocket(port)) {

      while(isRunning) {
        Socket player1 = listener.accept();
        System.out.println("[Gracz 1] dolaczyl");
        
        PrintWriter tempOut1 = new PrintWriter(player1.getOutputStream(), true);
        tempOut1.println("[SERWER] Polaczono jako Gracz 1 (CZARNY). Czekanie na przeciwnika...");

        Socket player2 = listener.accept();
        System.out.println("[Gracz 2] dolaczyl");
        PrintWriter tempOut2 = new PrintWriter(player2.getOutputStream(), true);
        tempOut2.println("[SERWER] Polaczono jako Gracz 2 (BIALY). Gra sie rozpoczyna");
        
        tempOut1.println("[SERWER] Przeciwnik dolaczyl. Gra sie rozpoczyna!");

        Game game = new Game(19);

        Long gameId = null;
        if (gameService != null) {
          gameId = gameService.startNewGame("HUMAN", "HUMAN");
          System.out.println("Utworzono gre w bazie o ID: " + gameId);
        }

        ClientHandler handler1 = new ClientHandler(player1, 1, game, gameService, gameId);
        ClientHandler handler2 = new ClientHandler(player2, 2, game, gameService, gameId);

        handler1.setOpponent(handler2);
        handler2.setOpponent(handler1);

        new Thread(handler1).start();
        new Thread(handler2).start();
      }

    } catch(IOException e) {
      e.printStackTrace();
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