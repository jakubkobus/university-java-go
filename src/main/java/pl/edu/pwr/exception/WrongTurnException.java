package pl.edu.pwr.exception;

import pl.edu.pwr.logic.Stone;

/**
 * Wyjątek rzucany gdy gracz próbuje wykonać ruch, ale nie jest jego tura.
 * 
 * Ten wyjątek przechwytuje zarówno aktualnego gracza, którego faktycznie jest
 * tura,
 * jak i gracza, który próbował wykonać ruch.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class WrongTurnException extends GoGameException {

  private final Stone currentPlayer;
  private final Stone attemptedPlayer;

  /**
   * Konstruuje nowy wyjątek dla próby ruchu w niewłaściwej turze.
   * 
   * @param current   gracz, którego aktualnie jest tura
   * @param attempted gracz, który próbował wykonać ruch
   */
  public WrongTurnException(Stone current, Stone attempted) {
    super(String.format("Wrong turn: current player is %s, but %s attempted move",
        current, attempted));
    this.currentPlayer = current;
    this.attemptedPlayer = attempted;
  }

  /**
   * Zwraca gracza, którego aktualnie jest tura.
   * 
   * @return aktualny gracz
   */
  public Stone getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Zwraca gracza, który próbował wykonać ruch.
   * 
   * @return gracz, który próbował wykonać ruch
   */
  public Stone getAttemptedPlayer() {
    return attemptedPlayer;
  }
}
