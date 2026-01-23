package pl.edu.pwr.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
 * Serwer nasłuchuje na skonfigurowanym porcie i przyjmuje połączenia od dwóch graczy
 * dla każdej partii gry w Go.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Server
 */
@SpringBootApplication
@ComponentScan(basePackages = "pl.edu.pwr")
@EnableJpaRepositories(basePackages = "pl.edu.pwr.database.repositories")
@EntityScan(basePackages = "pl.edu.pwr.database.entities")
public class ServerApp implements CommandLineRunner {
  
  private final Server server;

  /**
   * Konstruktor z wstrzykiwaniem zależności.
   * 
   * @param server komponent serwera zarządzany przez Springa
   */
  @Autowired
  public ServerApp(Server server) {
    this.server = server;
  }

  /**
   * Metoda główna aplikacji serwerowej.
   * 
   * Przepływ:
   * <ol>
   *   <li>Uruchamia kontekst Springa</li>
   *   <li>Uruchamia serwer na skonfigurowanym porcie</li>
   *   <li>Serwer wciąż nasłuchuje przychodzących połączeń od klientów</li>
   * </ol>
   * 
   * @param args argumenty wiersza poleceń (nieużywane)
   */
  public static void main(String[] args) {
    SpringApplication.run(ServerApp.class, args);
  }

  /**
   * Uruchamia serwer po zainicjalizowaniu kontekstu Springa.
   * 
   * @param args argumenty wiersza poleceń
   * @throws Exception jeśli wystąpi błąd podczas uruchamiania serwera
   */
  @Override
  public void run(String... args) throws Exception {
    server.start();
  }
}