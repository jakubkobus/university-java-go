package pl.edu.pwr.exception;

/**
 * Bazowy wyjątek dla wszystkich błędów związanych z grą Go.
 * 
 * Jest to korzeń hierarchii wyjątków dla błędów domenowych
 * i infrastrukturalnych w aplikacji gry Go.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class GoGameException extends Exception {

  /**
   * Konstruuje nowy wyjątek z określonym komunikatem szczegółowym.
   * 
   * @param message komunikat szczegółowy wyjaśniający błąd
   */
  public GoGameException(String message) {
    super(message);
  }

  /**
   * Konstruuje nowy wyjątek z określonym komunikatem szczegółowym i przyczyną.
   * 
   * @param message komunikat szczegółowy wyjaśniający błąd
   * @param cause   podstawowa przyczyna tego wyjątku
   */
  public GoGameException(String message, Throwable cause) {
    super(message, cause);
  }
}
