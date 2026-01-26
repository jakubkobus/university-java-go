package pl.edu.pwr.viewer;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Główna klasa aplikacji przeglądarki gier.
 * 
 * Jest to samodzielna aplikacja JavaFX wykorzystująca Spring Boot do połączenia
 * z bazą danych i przeglądania zapisanych gier.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
@SpringBootApplication(scanBasePackages = {"pl.edu.pwr.viewer", "pl.edu.pwr.database", "pl.edu.pwr.config"})
@EnableJpaRepositories(basePackages = "pl.edu.pwr.database.repositories")
@EntityScan(basePackages = "pl.edu.pwr.database.entities")
public class GameViewerApp {

  private static ConfigurableApplicationContext springContext;

  public static void main(String[] args) {
    springContext = new SpringApplicationBuilder(GameViewerApp.class)
        .headless(false)
        .run(args);

    Application.launch(GameViewerWindow.class, args);
  }

  public static ConfigurableApplicationContext getSpringContext() {
    return springContext;
  }
}
