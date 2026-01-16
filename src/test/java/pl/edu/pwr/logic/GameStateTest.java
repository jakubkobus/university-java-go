package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla stanów gry (game states) i przejść między fazami.
 * 
 * Testuje:
 * <ul>
 *   <li>Stan IN_PROGRESS (gra aktywna)</li>
 *   <li>Stan CLEANUP (faza sprzątania terytoriów)</li>
 *   <li>Stan FINISHED (gra skończona)</li>
 *   <li>Przejścia między stanami na podstawie pasów</li>
 *   <li>Usuwanie martwych kamieni w fazie CLEANUP</li>
 *   <li>Umieszczanie jeńców na terenie przeciwnika w fazie CLEANUP</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Game
 * @see GameState
 */
class GameStateTest {
  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game(9);
  }

  /**
   * Test przepływu pasów i zmian stanów gry.
   * 
   * Sprawdza:
   * <ol>
   *   <li>Gra zaczyna się w stanie IN_PROGRESS</li>
   *   <li>Jeden pas zmienia turę, ale pozostawia grę w IN_PROGRESS</li>
   *   <li>Ruch resetuje licznik pasów</li>
   *   <li>Dwa pasy z rzędu przechodzą grę do stanu CLEANUP</li>
   * </ol>
   */
  @Test
  void testPassFlow() {
    assertEquals(GameState.IN_PROGRESS, game.getState());

    game.pass();
    assertEquals(Stone.WHITE, game.getCurrentPlayer());
    assertEquals(GameState.IN_PROGRESS, game.getState());

    game.makeMove(0, 0);
    game.pass();
    assertEquals(GameState.IN_PROGRESS, game.getState());

    game.pass();
    game.pass();
    assertEquals(GameState.CLEANUP, game.getState(), "Dwa pasy z rzędu powinny uruchomić CLEANUP");
  }

  /**
   * Test walidacji usuwania martwych kamieni.
   * 
   * Sprawdza:
   * <ol>
   *   <li>Tylko martwe kamienie przeciwnika mogą być usunięte</li>
   *   <li>Gracz nie może usuwać swoich własnych kamieni</li>
   *   <li>Usunięty kamień zostaje dodany do licznika jeńców</li>
   *   <li>Nie można usuwać pól poza granicami lub już pótych</li>
   * </ol>
   */
  @Test
  void testRemoveDeadStoneValidation() {
    game.pass();
    game.pass();

    Game g = new Game(9);
    g.makeMove(0, 0);
    g.makeMove(1, 0);
    g.pass();
    g.pass();

    String result = g.removeDeadStone(1, 0, Stone.BLACK);
    assertEquals("OK", result);
    assertEquals(Stone.NONE, g.getBoard().get(1, 0));
    assertEquals(1, g.getBlackPrisoners(), "Czarny powinien dostać jeńca za usunięcie martwego kamienia");

    result = g.removeDeadStone(0, 0, Stone.BLACK);
    assertTrue(result.contains("ERR"), "Nie można usuwać własnych kamieni");

    result = g.removeDeadStone(5, 5, Stone.BLACK);
    assertTrue(result.contains("ERR"));
  }

  /**
   * Test umieszczania jeńców na terenie przeciwnika.
   * 
   * Sprawdza czy gracz może umieszczać jeńców na wolnych polach
   * w fazie CLEANUP do zaznaczania swoich terytoriów,
   * i czy liczba jeńców zmniejsza się po umieszczeniu.
   */
  @Test
  void testFillPrisoner() {
    Game g = new Game(9);

    g.makeMove(0, 0);
    g.makeMove(1, 0);
    g.makeMove(2, 0);
    g.makeMove(5, 5);
    g.makeMove(1, 1);

    assertEquals(Stone.NONE, g.getBoard().get(1, 0));
    assertEquals(1, g.getBlackPrisoners(), "Czarny powinien mieć 1 jeńca");

    g.pass();
    g.pass();

    boolean success = g.placePrisonerAsDead(5, 6, Stone.BLACK);

    assertTrue(success, "Powinno udać się postawić jeńca");
    assertEquals(Stone.WHITE, g.getBoard().get(5, 6), "Jeniec postawiony przez Czarnego powinien być Biały");
    assertEquals(0, g.getBlackPrisoners(), "Liczba jeńców powinna zmaleć");
  }
}