package pl.edu.pwr.exception;

/**
 * Wyjątek rzucany gdy wystąpi błąd podczas zapisywania lub pobierania danych
 * gry.
 * 
 * Ten wyjątek opakowuje błędy związane z bazą danych i dostarcza kontekst o
 * tym,
 * jaka operacja była próbowana gdy wystąpił błąd.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class GamePersistenceException extends GoGameException {

  /**
   * Konstruuje nowy wyjątek dla błędu trwałości gry.
   * 
   * @param message opis operacji trwałości, która się nie powiodła
   * @param cause   podstawowy wyjątek bazy danych lub I/O
   */
  public GamePersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Konstruuje nowy wyjątek z tylko komunikatem.
   * 
   * @param message opis operacji trwałości, która się nie powiodła
   */
  public GamePersistenceException(String message) {
    super(message);
  }
}
