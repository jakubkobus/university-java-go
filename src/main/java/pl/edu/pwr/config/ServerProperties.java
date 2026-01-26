package pl.edu.pwr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Właściwości konfiguracyjne dla serwera gry Go.
 * 
 * Właściwości są wczytywane z application.properties z prefiksem "game.server".
 * Zawiera walidację aby zapewnić, że wartości konfiguracji są w poprawnych
 * zakresach.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
@ConfigurationProperties(prefix = "game.server")
@Validated
public class ServerProperties {

  /** Minimalny poprawny numer portu (powyżej portów uprzywilejowanych) */
  private static final int MIN_PORT = 1024;

  /** Maksymalny poprawny numer portu */
  private static final int MAX_PORT = 65535;

  /** Domyślny port serwera */
  private static final int DEFAULT_PORT = 8080;

  @Min(value = MIN_PORT, message = "Port must be at least " + MIN_PORT)
  @Max(value = MAX_PORT, message = "Port must be at most " + MAX_PORT)
  private int port = DEFAULT_PORT;

  private boolean running = true;

  /**
   * Zwraca port serwera.
   * 
   * @return numer portu (zwalidowany aby być między 1024 a 65535)
   */
  public int getPort() {
    return port;
  }

  /**
   * Ustawia port serwera.
   * 
   * @param port numer portu (musi być między 1024 a 65535)
   * @throws IllegalArgumentException jeśli port jest poza poprawnym zakresem
   */
  public void setPort(int port) {
    if (port < MIN_PORT || port > MAX_PORT) {
      throw new IllegalArgumentException(
          String.format("Port must be between %d and %d, got: %d",
              MIN_PORT, MAX_PORT, port));
    }
    this.port = port;
  }

  /**
   * Sprawdza czy serwer powinien być uruchomiony.
   * 
   * @return true jeśli serwer powinien działać, false w przeciwnym razie
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Ustawia czy serwer powinien być uruchomiony.
   * 
   * @param running true aby uruchomić serwer, false aby go zatrzymać
   */
  public void setRunning(boolean running) {
    this.running = running;
  }
}
