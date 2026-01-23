package pl.edu.pwr.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import pl.edu.pwr.database.service.GameService;

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
@SpringBootApplication
@ComponentScan(basePackages = "pl.edu.pwr")
@EnableJpaRepositories(basePackages = "pl.edu.pwr.database.repositories")
@EntityScan(basePackages = "pl.edu.pwr.database.entities")
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
    ConfigurableApplicationContext context = SpringApplication.run(ServerApp.class, args);
    GameService gameService = context.getBean(GameService.class);
    Server.getInstance().setGameService(gameService);
    Server.getInstance().start(8080);
  }
}