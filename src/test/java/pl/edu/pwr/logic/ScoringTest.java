package pl.edu.pwr.logic;

import org.junit.jupiter.api.Test;
import pl.edu.pwr.logic.scoring.IScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringStrategy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla strategii punktacji gry w Go.
 * 
 * Testuje obliczanie wyniku końcowego gry:
 * <ul>
 *   <li>Liczenie punktów za terytoria</li>
 *   <li>Liczenie punktów za jeńców (przechwycone kamienie)</li>
 *   <li>Rozpoznawanie terytoriów neutralnych</li>
 *   <li>Określenie zwycięzcy na podstawie punktacji</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ScoringStrategy
 * @see GameResult
 */
class ScoringTest {

  /**
   * Test liczenia punktów za proste terytoria.
   * 
   * Sprawdza czy system prawidłowo liczy pola kontrolowane
   * przez każdego gracza na podstawie pozycji ich kamieni.
   */
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

  /**
   * Test liczenia punktów ze względu na jeńców.
   * 
   * Sprawdza czy system prawidłowo dodaje do wyniku
   * przechwycone kamienie (jeńców) każdego gracza.
   */
  @Test
  void testScoreWithPrisoners() {
    Board board = new Board(9);

    IScoringStrategy strategy = new ScoringStrategy();
    GameResult result = strategy.score(board, 5, 2);

    assertEquals(5, result.blackScore());
    assertEquals(2, result.whiteScore());
  }

  /**
   * Test rozpoznawania terytoriów neutralnych.
   * 
   * Sprawdza czy system prawidłowo identyfikuje terytoria
   * kontrolowane przez obu graczy jednocześnie (neutralne)
   * i nie przyznaje ich żadnemu z graczy.
   */
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