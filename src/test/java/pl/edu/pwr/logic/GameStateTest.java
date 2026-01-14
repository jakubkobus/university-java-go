package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game(9);
  }

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