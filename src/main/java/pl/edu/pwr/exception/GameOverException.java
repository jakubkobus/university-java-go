package pl.edu.pwr.exception;

/**
 * Wyjątek rzucany gdy gracz próbuje wykonać ruch w grze, która już się
 * zakończyła.
 * 
 * Ten wyjątek zapobiega dalszym ruchom po tym jak obaj gracze spasowali lub
 * gra została jawnie zakończona.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class GameOverException extends GoGameException {

  /**
   * Konstruuje nowy wyjątek wskazujący, że gra już się zakończyła.
   */
  public GameOverException() {
    super("Game is already over");
  }

  /**
   * Konstruuje nowy wyjątek z własnym komunikatem.
   * 
   * @param message komunikat szczegółowy wyjaśniający dlaczego gra się zakończyła
   */
  public GameOverException(String message) {
    super(message);
  }
}
