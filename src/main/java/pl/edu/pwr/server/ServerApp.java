package pl.edu.pwr.server;

/**
 * Punkt wejścia aplikacji serwera gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Inicjalizację serwera gry</li>
 *   <li>Uruchomienie nasłuchiwania na połączenia klientów</li>
 *   <li>Zarządzanie pętlą główną serwera</li>
 * </ul>
 * 
 * Serwer nasłuchuje na porcie 8080 i przyjmuje połączenia od dwóch graczy
 * dla każdej partii gry w Go.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Server
 */
public class ServerApp {
  
  /**
   * Metoda główna aplikacji serwerowej.
   * 
   * Przepływ:
   * <ol>
   *   <li>Pobiera singleton instancję serwera</li>
   *   <li>Uruchamia serwer na porcie 8080</li>
   *   <li>Serwer wciąż nasłuchuje przychodzących połączeń od klientów</li>
   * </ol>
   * 
   * @param args argumenty wiersza poleceń (nieużywane)
   */
  public static void main(String[] args) {
    Server.getInstance().start(8080);
  }
}