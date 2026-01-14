package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {
  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game(19);
  }

  @Test
  void testBasicAlternatingTurns() {
    assertEquals(Stone.BLACK, game.getCurrentPlayer());
    assertTrue(game.makeMove(0, 0));

    assertEquals(Stone.WHITE, game.getCurrentPlayer());
    assertTrue(game.makeMove(0, 1));

    assertEquals(Stone.BLACK, game.getCurrentPlayer());
  }

  @Test
  void testCannotPlayOnOccupiedIntersection() {
    game.makeMove(5, 5);
    assertFalse(game.makeMove(5, 5));

    assertEquals(Stone.WHITE, game.getCurrentPlayer());
  }

  @Test
  void testCannotPlayOutOfBounds() {
    assertFalse(game.makeMove(-1, 0));
    assertFalse(game.makeMove(0, 19));
    assertFalse(game.makeMove(19, 0));
  }

  @Test
  void testCaptureSingleStoneInCorner() {
    game.makeMove(0, 0);
    game.makeMove(1, 0);
    game.makeMove(10, 10);
    game.makeMove(0, 1);

    assertEquals(Stone.NONE, game.getBoard().get(0, 0), "Kamień na 0,0 powinien zniknąć");
    assertEquals(1, game.getWhitePrisoners(), "Biały powinien mieć 1 jeńca");
  }

  @Test
  void testCaptureGroupOfStones() {
    game.makeMove(0, 0);
    game.makeMove(1, 0);
    game.makeMove(0, 1);
    game.makeMove(1, 1);
    game.makeMove(10, 10);
    game.makeMove(0, 2);

    assertEquals(Stone.NONE, game.getBoard().get(0, 0));
    assertEquals(Stone.NONE, game.getBoard().get(0, 1));
    assertEquals(2, game.getWhitePrisoners());
  }

  @Test
  void testSuicideIsForbidden() {
    game = new Game(9);
    game.makeMove(10, 10);
    game.makeMove(1, 0);
    game.makeMove(2, 0);
    game.makeMove(0, 1);
    game.makeMove(0, 2);
    game.makeMove(1, 1);
    game.makeMove(1, 2);

    Game suicideGame = new Game(9);
    suicideGame.makeMove(0, 1);
    suicideGame.makeMove(0, 0);
    suicideGame.makeMove(1, 0);

    assertFalse(suicideGame.makeMove(0, 0), "Ruch samobójczy powinien być odrzucony");
  }

  @Test
  void testSuicideThatCapturesIsAllowed() {
    Game g = new Game(9);
    g.makeMove(0, 1);
    g.makeMove(0, 2);
    g.makeMove(1, 1);
    g.makeMove(1, 2);
    g.makeMove(1, 0);
    g.makeMove(5, 5);

    g.makeMove(5, 6);
    g.makeMove(0, 0);
  }

  @Test
  void testKoRule() {
    game.makeMove(2, 1);
    game.makeMove(1, 2);
    game.makeMove(3, 2);
    game.makeMove(2, 3);
    game.makeMove(2, 2);

    game.makeMove(1, 0);
    game.makeMove(1, 1);

    Game koGame = new Game(19);
    koGame.makeMove(1, 0);
    koGame.makeMove(2, 0);
    koGame.makeMove(0, 1);
    koGame.makeMove(3, 1);
    koGame.makeMove(1, 2);
    koGame.makeMove(2, 2);

    koGame.makeMove(2, 1);
    koGame.makeMove(1, 1);

    assertFalse(koGame.makeMove(2, 1), "Zasada Ko powinna zabronić natychmiastowego odbicia");
  }
}