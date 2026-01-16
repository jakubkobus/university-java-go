package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla klasy Game.
 * 
 * Testuje główną logikę gry w Go:
 * <ul>
 *   <li>Zmianę tur graczy</li>
 *   <li>Umieszczanie kamieni na planszy</li>
 *   <li>Zbijanie (capture) kamieni przeciwnika</li>
 *   <li>Walidację ruchów samobójczych (suicide moves)</li>
 *   <li>Ko rule (niedozwolone powtórzenie pozycji)</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Game
 */
class GameTest {
  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game(19);
  }

  /**
   * Test sprawdzający czy czarne gracze zaczynają grę.
   * 
   * Weryfikuje czy aktualnym graczem na początku gry jest Stone.BLACK.
   */
  @Test
  void testBlackStartsGame() {
    assertEquals(Stone.BLACK, game.getCurrentPlayer(), "Czarne powinny zaczynać grę");
  }

  /**
   * Test zmian tury pomiędzy graczami.
   * 
   * Sprawdza czy tura zmienia się prawidłowo pomiędzy czarnymi a białymi
   * po każdym wykonanym ruchu.
   */
  @Test
  void testTurnChangesAfterMove() {
    game.makeMove(0, 0);
    assertEquals(Stone.WHITE, game.getCurrentPlayer(), "Po ruchu Czarnych powinna być tura Białych");

    game.makeMove(0, 1);
    assertEquals(Stone.BLACK, game.getCurrentPlayer(), "Po ruchu Białych powinna być tura Czarnych");
  }

  /**
   * Test zakazania umieszczenia kamienia na zajętym polu.
   * 
   * Sprawdza czy system nie pozwoli na postawienie kamienia na polu,
   * na którym już znajduje się inny kamień, i czy tura nie zmienia się
   * po nieudanym ruchu.
   */
  @Test
  void testCannotPlaceOnOccupiedSpot() {
    game.makeMove(5, 5);
    boolean success = game.makeMove(5, 5);

    assertFalse(success, "Nie powinno się dać postawić kamienia na zajętym polu");
    assertEquals(Stone.BLACK, game.getBoard().get(5, 5), "Kamień nie powinien zmienić koloru");
    assertEquals(Stone.WHITE, game.getCurrentPlayer(), "Tura nie powinna się zmienić po błędnym ruchu");
  }

  /**
   * Test zbijania samotnego kamienia w rogu planszy.
   * 
   * Sprawdza czy system prawidłowo rozpoznaje i usuwa zbite kamienie
   * (kamienie bez wciąż dostępnych libercies - wolnych pól otaczających grupę).
   */
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

  /**
   * Test zakazania ruchu samobójczego.
   * 
   * Sprawdza czy system zabrania ruchów samobójczych, czyli umieszczenia kamienia
   * na polu, które nie posiada żadnych liberties i nie bije żadnych kamieni przeciwnika.
   */
  @Test
  void suicideMoveIsForbidden() {
        Game game = new Game(19);

        game.makeMove(1, 0);
        game.makeMove(10, 10);
        game.makeMove(0, 1);
        game.makeMove(11, 11);
        game.makeMove(2, 1);
        game.makeMove(12, 12);
        game.makeMove(1, 2);

        boolean allowed = game.makeMove(1, 1);

        assertFalse(allowed, "Ruch samobójczy powinien być zabroniony");
    }

    /**
     * Test pozwalający ruch samobójczy, który bije kamienie przeciwnika.
     * 
     * Sprawdza czy system pozwala na ruch samobójczy,
     * jeśli jednocześnie bije (zbija) kamienie przeciwnika.
     * W takim przypadku kamienie własne nie są usuwane, bo po zebiciu
     * grupa posiada liberties.
     */
    @Test
    void suicideThatCapturesOpponentIsAllowed() {
        Game game = new Game(19);

        game.makeMove(1, 0);
        game.makeMove(18, 18);
        game.makeMove(1, 1);
        game.makeMove(10, 10);
        game.makeMove(1, 2);
        game.makeMove(12, 12);
        game.makeMove(0, 2);

        game.makeMove(0, 0);

        boolean allowed = game.makeMove(0, 1);

        assertTrue(allowed, "Ruch samobójczy, który bije przeciwnika, jest dozwolony");

        Board board = game.getBoard();
        assertEquals(Stone.NONE, board.get(0, 0), "Kamień biały powinien zostać zbity");
    }


}