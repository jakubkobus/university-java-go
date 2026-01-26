package pl.edu.pwr.exception;

/**
 * Wyjątek rzucany gdy gracz próbuje wykonać nieprawidłowy ruch na planszy Go.
 * 
 * Ten wyjątek przechwytuje współrzędne próbowanego ruchu i dostarcza
 * szczegółowy powód dlaczego ruch był nieprawidłowy (np. pozycja zajęta, reguła
 * samobójstwa,
 * naruszenie reguły ko, poza granicami).
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class InvalidMoveException extends GoGameException {

  private final int x;
  private final int y;
  private final String reason;

  /**
   * Konstruuje nowy wyjątek dla próby nieprawidłowego ruchu.
   * 
   * @param x      współrzędna x próbowanego ruchu
   * @param y      współrzędna y próbowanego ruchu
   * @param reason szczegółowe wyjaśnienie dlaczego ruch jest nieprawidłowy
   */
  public InvalidMoveException(int x, int y, String reason) {
    super(String.format("Invalid move at (%d, %d): %s", x, y, reason));
    this.x = x;
    this.y = y;
    this.reason = reason;
  }

  /**
   * Zwraca współrzędną x nieprawidłowego ruchu.
   * 
   * @return współrzędna x
   */
  public int getX() {
    return x;
  }

  /**
   * Zwraca współrzędną y nieprawidłowego ruchu.
   * 
   * @return współrzędna y
   */
  public int getY() {
    return y;
  }

  /**
   * Zwraca powód dlaczego ruch był nieprawidłowy.
   * 
   * @return szczegółowy powód
   */
  public String getReason() {
    return reason;
  }
}
