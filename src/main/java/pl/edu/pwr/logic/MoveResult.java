package pl.edu.pwr.logic;

/**
 * Obiekt wyniku dla prób wykonania ruchu w grze Go.
 * 
 * Zamiast zwracać prosty boolean, ten record dostarcza szczegółowych
 * informacji o tym, dlaczego ruch się powiódł lub nie powiódł, czyniąc
 * debugowanie i informacje zwrotne dla użytkownika znacznie łatwiejszymi.
 * 
 * @param success      czy ruch został pomyślnie wykonany
 * @param errorMessage szczegółowy opis dlaczego ruch się nie powiódł (null
 *                     jeśli sukces)
 * @param reason       wyliczony powód niepowodzenia (SUCCESS jeśli ruch się
 *                     powiódł)
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public record MoveResult(boolean success, String errorMessage, MoveFailureReason reason) {

  /**
   * Wyliczenie możliwych powodów niepowodzenia ruchu.
   */
  public enum MoveFailureReason {
    /** Ruch został pomyślnie wykonany */
    SUCCESS,
    /** Gra już się zakończyła */
    GAME_OVER,
    /** Współrzędne są poza granicami planszy */
    OUT_OF_BOUNDS,
    /** Pozycja jest już zajęta przez kamień */
    OCCUPIED,
    /** Ruch byłby samobójstwem (postawienie kamienia bez liberties) */
    SUICIDE,
    /** Ruch narusza regułę ko (natychmiastowe powtórzenie stanu planszy) */
    KO_RULE
  }

  /**
   * Tworzy wynik sukcesu.
   * 
   * @return MoveResult wskazujący, że ruch się powiódł
   */
  public static MoveResult ofSuccess() {
    return new MoveResult(true, null, MoveFailureReason.SUCCESS);
  }

  /**
   * Tworzy wynik błędu z konkretnym powodem i komunikatem.
   * 
   * @param reason  wyliczony powód niepowodzenia
   * @param message szczegółowy komunikat błędu zrozumiały dla człowieka
   * @return MoveResult wskazujący, że ruch się nie powiódł
   */
  public static MoveResult ofError(MoveFailureReason reason, String message) {
    return new MoveResult(false, message, reason);
  }
}
