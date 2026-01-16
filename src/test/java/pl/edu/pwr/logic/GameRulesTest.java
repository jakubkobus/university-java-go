package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla reguł gry w Go.
 * 
 * Testuje zasady gry:
 * <ul>
 *   <li>Zmiana tur pomiędzy graczami</li>
 *   <li>Niedozwolone umieszczenie na zajętych polach</li>
 *   <li>Niedozwolone ruchy poza granicami planszy</li>
 *   <li>Zbijanie (capture) pojedynczych kamieni</li>
 *   <li>Zbijanie grup kamieni</li>
 *   <li>Zakaz ruchów samobójczych (suicide moves)</li>
 *   <li>Pozwolenie na ruchy samobójcze zbijające kamienie</li>
 *   <li>Ko rule (niedozwolone natychmiastowe powtórzenie pozycji)</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Game
 */
class GameRulesTest {
  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game(19);
  }

  /**
   * Test alternacyjnej zmiany tur pomiędzy graczami.
   * 
   * Sprawdza czy gracze zmienią się na siebie po każdym ruchu,
   * zaczynając od czarnych.
   */
  @Test
  void testBasicAlternatingTurns() {
    assertEquals(Stone.BLACK, game.getCurrentPlayer());
    assertTrue(game.makeMove(0, 0));

    assertEquals(Stone.WHITE, game.getCurrentPlayer());
    assertTrue(game.makeMove(0, 1));

    assertEquals(Stone.BLACK, game.getCurrentPlayer());
  }

  /**
   * Test zakazania umieszczenia kamienia na zajętym polu.
   * 
   * Sprawdza czy system nie pozwoli na postawienie drugiego kamienia
   * na tym samym polu, i czy tura zmienia się prawidłowo.
   */
  @Test
  void testCannotPlayOnOccupiedIntersection() {
    game.makeMove(5, 5);
    assertFalse(game.makeMove(5, 5));

    assertEquals(Stone.WHITE, game.getCurrentPlayer());
  }

  /**
   * Test zakazania umieszczenia kamienia poza granicami planszy.
   * 
   * Sprawdza czy system odrzuci ruchy z ujemnymi współrzędnymi
   * lub współrzędnymi większymi od wielkości planszy.
   */
  @Test
  void testCannotPlayOutOfBounds() {
    assertFalse(game.makeMove(-1, 0));
    assertFalse(game.makeMove(0, 19));
    assertFalse(game.makeMove(19, 0));
  }

  /**
   * Test zbijania samotnego kamienia w rogu planszy.
   * 
   * Sprawdza czy system prawidłowo zbija samotny kamień
   * i dodaje go do licznika jeńców.
   */
  @Test
  void testCaptureSingleStoneInCorner() {
    game.makeMove(0, 0);
    game.makeMove(1, 0);
    game.makeMove(10, 10);
    game.makeMove(0, 1);

    assertEquals(Stone.NONE, game.getBoard().get(0, 0), "Kamień na 0,0 powinien zniknąć");
    assertEquals(1, game.getWhitePrisoners(), "Biały powinien mieć 1 jeńca");
  }

  /**
   * Test zbijania grupy kamieni połączonych ze sobą.
   * 
   * Sprawdza czy system zbija całą grupę kamieni
   * gdy ostatnia liberty grupy zostaje usunięta.
   */
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

  /**
   * Test zakazania ruchu samobójczego.
   * 
   * Sprawdza czy system zabrania umieszczenia kamienia na polu,
   * które nie posiada liberties i nie zbija żadnych kamieni przeciwnika.
   */
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

  /**
   * Test pozwalający ruch samobójczy, który zbija kamienie.
   * 
   * Sprawdza czy system pozwala na ruch samobójczy
   * jeśli jednocześnie zbija kamienie przeciwnika,
   * co daje grupie liberties.
   */
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

  /**
   * Test Ko rule (niedozwolone natychmiastowe powtórzenie pozycji).
   * 
   * Sprawdza czy system zabrania natychmiastowego odbicia (re-capture),
   * które by przywróciło poprzednią pozycję planszy.
   * Ko rule zapobiega nieskończonym pętlom zbijania się nawzajem.
   */
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