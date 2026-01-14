package pl.edu.pwr.logic;

import org.junit.jupiter.api.Test;
import pl.edu.pwr.logic.scoring.IScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringStrategy;
import static org.junit.jupiter.api.Assertions.*;

class ScoringTest {

  @Test
  void testScoreSimpleTerritory() {
    Board board = new Board(9);
    board.placeStone(0, 0, Stone.BLACK);
    board.placeStone(0, 1, Stone.BLACK);
    board.placeStone(1, 1, Stone.BLACK);
    board.placeStone(2, 1, Stone.BLACK);
    board.placeStone(2, 0, Stone.BLACK);

    board.placeStone(8, 8, Stone.WHITE);

    IScoringStrategy strategy = new ScoringStrategy();
    GameResult result = strategy.score(board, 0, 0);

    assertEquals(1, result.blackScore(), "Czarny powinien mieć 1 pkt za terytorium");
    assertEquals(0, result.whiteScore());
  }

  @Test
  void testScoreWithPrisoners() {
    Board board = new Board(9);

    IScoringStrategy strategy = new ScoringStrategy();
    GameResult result = strategy.score(board, 5, 2);

    assertEquals(5, result.blackScore());
    assertEquals(2, result.whiteScore());
  }

  @Test
  void testNeutralTerritory() {
    Board board = new Board(9);
    board.placeStone(0, 0, Stone.BLACK);
    board.placeStone(2, 0, Stone.WHITE);

    IScoringStrategy strategy = new ScoringStrategy();
    GameResult result = strategy.score(board, 0, 0);

    assertEquals(0, result.blackScore());
    assertEquals(0, result.whiteScore());
  }
}