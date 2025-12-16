package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {
  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game(19);
  }

  @Test
  void testBlackStartsGame() {
    assertEquals(Stone.BLACK, game.getCurrentPlayer(), "Czarne powinny zaczynać grę");
  }

  @Test
  void testTurnChangesAfterMove() {
    game.makeMove(0, 0);
    assertEquals(Stone.WHITE, game.getCurrentPlayer(), "Po ruchu Czarnych powinna być tura Białych");

    game.makeMove(0, 1);
    assertEquals(Stone.BLACK, game.getCurrentPlayer(), "Po ruchu Białych powinna być tura Czarnych");
  }

  @Test
  void testCannotPlaceOnOccupiedSpot() {
    game.makeMove(5, 5);
    boolean success = game.makeMove(5, 5);

    assertFalse(success, "Nie powinno się dać postawić kamienia na zajętym polu");
    assertEquals(Stone.BLACK, game.getBoard().get(5, 5), "Kamień nie powinien zmienić koloru");
    assertEquals(Stone.WHITE, game.getCurrentPlayer(), "Tura nie powinna się zmienić po błędnym ruchu");
  }

  @Test
  void testCapturingStoneInCorner() {
    game.makeMove(0, 0);
    game.makeMove(0, 1);
    game.makeMove(19, 19);
    game.makeMove(5, 5);

    game = new Game(9);

    game.makeMove(0, 1);
    game.makeMove(0, 0);
    game.makeMove(1, 0);

    assertEquals(Stone.NONE, game.getBoard().get(0, 0), "Biały kamień w rogu powinien zostać zbity (zniknąć)");
  }
}