package pl.edu.pwr.server;

/**
 * Scentralizowane komunikaty błędów i informacji dla serwera gry Go.
 * 
 * Zapewnia spójne komunikaty we wszystkich komendach i obsługach serwera.
 * Wszystkie komunikaty są po polsku, aby pasowały do istniejącego interfejsu
 * gry.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public final class ServerMessages {

  /**
   * Prywatny konstruktor zapobiegający tworzeniu instancji.
   * To jest klasa narzędziowa zawierająca tylko statyczne stałe.
   */
  private ServerMessages() {
    throw new AssertionError("Cannot instantiate ServerMessages");
  }

  /** Błąd: Nie jest tura gracza */
  public static final String ERROR_NOT_YOUR_TURN = "ERR To nie Twoja tura!";

  /** Błąd: Nieprawidłowy format współrzędnych */
  public static final String ERROR_INVALID_COORDINATES = "ERR Wspolrzedne musza byc liczbami";

  /** Błąd: Nieprawidłowy format komendy */
  public static String errorWrongFormat(String usage) {
    return "ERR Zly format. Uzyj: " + usage;
  }

  /** Błąd: Gra już się zakończyła */
  public static final String ERROR_GAME_OVER = "ERR Gra juz sie zakonczyla";

  /** Błąd: Współrzędne poza planszą */
  public static final String ERROR_OUT_OF_BOUNDS = "ERR Wspolrzedne poza plansza";

  /** Błąd: Pozycja już zajęta */
  public static final String ERROR_OCCUPIED = "ERR To pole jest juz zajete";

  /** Błąd: Ruch samobójczy */
  public static final String ERROR_SUICIDE = "ERR Ruch niedozwolony (samobojstwo - grupa nie miala by wolnych pol)";

  /** Błąd: Naruszenie reguły Ko */
  public static final String ERROR_KO_RULE = "ERR Ruch niedozwolony (naruszenie reguly Ko - powtorzenie pozycji)";

  /** Błąd: Nie w fazie czyszczenia */
  public static final String ERROR_NOT_CLEANUP = "ERR Mozesz usuwac kamienie tylko po zakonczeniu fazy ruchow (2x PASS)";

  /** Błąd: Nie można postawić jeńca */
  public static final String ERROR_CANNOT_PLACE_PRISONER = "ERR Nie mozesz tu postawic jenca (brak jencow lub pole zajete)";

  /** Błąd: Brak kamienia do usunięcia */
  public static final String ERROR_NO_STONE = "ERR Nie ma tam kamienia do usuniecia";

  /** Info: Ruch wykonany */
  public static String infoMoveExecuted(int x, int y) {
    return String.format("INFO Wykonano ruch: %d %d", x, y);
  }

  /** Info: Przeciwnik wykonał ruch */
  public static String infoOpponentMoved(int x, int y) {
    return String.format("INFO Przeciwnik wykonal ruch: %d %d", x, y);
  }

  /** Info: Gracz spasował */
  public static final String INFO_PASSED = "INFO Spasowales";

  /** Info: Przeciwnik spasował */
  public static final String INFO_OPPONENT_PASSED = "INFO Przeciwnik spasowal";

  /** Info: Martwy kamień usunięty */
  public static final String INFO_DEAD_STONE_REMOVED = "INFO Usunieto martwy kamien";

  /** Info: Przeciwnik usunął martwy kamień */
  public static final String INFO_OPPONENT_REMOVED_DEAD = "INFO Przeciwnik usunal martwy kamien";

  /** Info: Jeniec postawiony */
  public static final String INFO_PRISONER_PLACED = "INFO Postawiono jenca na terytorium przeciwnika";

  /** Info: Przeciwnik postawił jeńca */
  public static final String INFO_OPPONENT_PLACED_PRISONER = "INFO Przeciwnik postawil jenca";

  /** Info: Gracz się poddał */
  public static final String INFO_SURRENDERED = "INFO Poddales sie. Przegrales";

  /** Info: Przeciwnik się poddał */
  public static final String INFO_OPPONENT_SURRENDERED = "INFO Przeciwnik sie poddal";

  /** Serwer: Połączono jako gracz 1 (CZARNY) */
  public static final String SERVER_CONNECTED_PLAYER1 = "[SERWER] Polaczono jako Gracz 1 (CZARNY). Czekanie na przeciwnika...";

  /** Serwer: Połączono jako gracz 2 (BIAŁY) */
  public static final String SERVER_CONNECTED_PLAYER2 = "[SERWER] Polaczono jako Gracz 2 (BIALY). Gra sie rozpoczyna";

  /** Serwer: Przeciwnik dołączył */
  public static final String SERVER_OPPONENT_JOINED = "[SERWER] Przeciwnik dolaczyl. Gra sie rozpoczyna!";
}
