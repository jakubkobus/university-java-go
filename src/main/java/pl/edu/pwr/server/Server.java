package pl.edu.pwr.server;

import pl.edu.pwr.logic.Game;
import java.io.IOException;
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
  private ClientHandler waitingHandler = null;

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
      System.out.println("Serwer startuje na porcie " + port);

      try (ServerSocket listener = new ServerSocket(port)) {

          while (isRunning) {
              try {
                  Socket socket = listener.accept();
                  System.out.println("[SERWER] Nowe polaczenie przychodzace...");

                  synchronized (this) {
                      if (waitingHandler != null) {
                          if (!waitingHandler.isConnected()) {
                              System.out.println("[SERWER] WaitingHandler rozlaczony. Usuwam.");
                              waitingHandler = null;
                          } else if (waitingHandler.getOpponent() != null) {
                              System.out.println("[SERWER] WaitingHandler juz gra. Usuwam z kolejki.");
                              waitingHandler = null;
                          }
                      }

                      if (waitingHandler != null) {
                          System.out.println("[SERWER] Laczenie z oczekujacym graczem.");

                          Game game = waitingHandler.getGame();
                          ClientHandler player2 = new ClientHandler(socket, 2, game);

                          waitingHandler.setOpponent(player2);
                          player2.setOpponent(waitingHandler);

                          new Thread(player2).start();

                          waitingHandler.sendMessage("OPPONENT_JOINED");
                          waitingHandler.sendMessage("INFO Dolaczyl przeciwnik! Zaczynamy.");
                          player2.sendMessage("INFO Dolaczyles do gry. Zaczynamy.");

                          waitingHandler.sendBoard();
                          player2.sendBoard();

                          waitingHandler = null;

                      } else {
                          System.out.println("[SERWER] Tworzenie nowej gry (Gracz 1).");

                          Game newGame = new Game(19);
                          ClientHandler player1 = new ClientHandler(socket, 1, newGame);

                          waitingHandler = player1;

                          new Thread(player1).start();
                          player1.sendMessage("INFO Czekaj na przeciwnika lub wybierz bota.");
                      }
                  }

              } catch (Exception e) {
                  System.err.println("!!! [SERWER] Blad przy obsludze polaczenia: " + e.getMessage());
                  e.printStackTrace();
              }
          }

      } catch (IOException e) {
          System.err.println("Krytyczny blad portu/serwera: " + e.getMessage());
          e.printStackTrace();
      }
  }
}