package pl.edu.pwr.logic;

/**
 * Stałe używane w grze Go.
 * 
 * Centralizuje wszystkie magiczne liczby i wartości konfiguracyjne,
 * czyniąc kod bardziej czytelnym i łatwiejszym w utrzymaniu.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public final class GameConstants {

  /**
   * Prywatny konstruktor zapobiegający tworzeniu instancji.
   * To jest klasa narzędziowa zawierająca tylko statyczne stałe.
   */
  private GameConstants() {
    throw new AssertionError("Cannot instantiate GameConstants");
  }

  /** Standardowy rozmiar planszy turniejowej (najpopularniejszy) */
  public static final int STANDARD_BOARD_SIZE = 19;

  /** Średni rozmiar planszy (gry dla średniozaawansowanych) */
  public static final int MEDIUM_BOARD_SIZE = 13;

  /** Mały rozmiar planszy (gry dla początkujących) */
  public static final int SMALL_BOARD_SIZE = 9;

  /** Liczba kolejnych pasów wymaganych do zakończenia gry */
  public static final int PASSES_TO_END_GAME = 2;

  /** Komi (punkty kompensacyjne dla białego gracza) - wartość standardowa */
  public static final double STANDARD_KOMI = 6.5;

  /**
   * Przesunięcie dla konwersji między współrzędnymi użytkownika (1-indeksowane) a
   * wewnętrznymi (0-indeksowane)
   */
  public static final int COORDINATE_OFFSET = 1;

  /** Specjalna wartość współrzędnej dla ruchów pass w bazie danych */
  public static final int PASS_COORDINATE = -1;

  /** Prefiks komunikatów błędów */
  public static final String ERROR_PREFIX = "ERR";

  /** Prefiks komunikatów informacyjnych */
  public static final String INFO_PREFIX = "INFO";

  /** Prefiks komunikatu zakończenia gry */
  public static final String GAME_OVER_PREFIX = "GAME_OVER";
}
