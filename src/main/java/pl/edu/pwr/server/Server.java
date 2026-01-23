package pl.edu.pwr.server;

import pl.edu.pwr.logic.Game;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import pl.edu.pwr.logic.Game;
import pl.edu.pwr.database.service.GameService;

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
 * Implementuje wzorzec Singleton, zapewniając tylko jedną instancję serwera.
 * Serwer przyjmuje dwóch graczy, tworzy grę o rozmiarze 19x19 i uruchamia ich w osobnych wątkach.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ClientHandler
 * @see Game
 */
public class Server {
  
  /** Singleton instancja serwera */
  private static Server instance;
  
  /** Flaga określająca, czy serwer powinien być uruchomiony */
  private volatile boolean isRunning = true;

  /** Serwis do zarządzania grami w bazie danych */
  private GameService gameService;

  /**
   * Konstruktor prywatny dla wzorca Singleton.
   */
  private Server() {
  }

  /**
   * Zwraca singleton instancję serwera.
   * Implementuje thread-safe lazy initialization.
   * 
   * @return singleton instancja serwera
   */
  public static Server getInstance() {
    if (instance == null)
      synchronized (Server.class) {
        if (instance == null)
          instance = new Server();
      }
    return instance;
  }

  /**
   * Ustawia serwis do zarządzania grami w bazie danych.
   * 
   * @param gameService instancja GameService
   */
  public void setGameService(GameService gameService) {
    this.gameService = gameService;
  }

  /**
   * Uruchamia serwer na podanym porcie.
   * 
   * Przepływ:
   * <ol>
   *   <li>Tworzy ServerSocket na podanym porcie</li>
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
   * 
   * @param port numer portu, na którym serwer będzie nasłuchiwać (np. 8080)
   */
  public void start(int port) {
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
}