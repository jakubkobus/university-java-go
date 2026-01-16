package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla fazy cleanup (sprzątania) w grze Go.
 * 
 * Testuje funkcjonalność fazy końcowej gry:
 * <ul>
 *   <li>Przejście z fazy IN_PROGRESS do CLEANUP po dwóch pasach</li>
 *   <li>Przejście z fazy CLEANUP do FINISHED po dwóch pasach w cleanup</li>
 *   <li>Usuwanie martwych kamieni (removeDeadStone)</li>
 *   <li>Umieszczanie jeńców na terenie przeciwnika (placePrisonerAsDead)</li>
 *   <li>Liczenie jeńców i aktualizacja stanu planszy</li>
 * </ul>
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Game
 * @see GameState
 */
public class GameCleanupTest {
    private Game game;

    @BeforeEach
    public void setUp() {
        game = new Game(19);
    }

    /**
     * Test pełnego procesu cleanup i umieszczania jeńców.
     * 
     * Przepływ:
     * <ol>
     *   <li>Ustawia pozycję z martwymi kamieniami na planszy</li>
     *   <li>Sprawdza że gra jest w stanie IN_PROGRESS</li>
     *   <li>Gracze spasują 2 razy, co przechodzi grę do stanu CLEANUP</li>
     *   <li>Gracze usuwają martwe kamienie (removeDeadStone)</li>
     *   <li>Gracze umieszczają jeńców na terenie przeciwnika (placePrisonerAsDead)</li>
     *   <li>Gracze spasują 2 razy w CLEANUP, co kończy grę (FINISHED)</li>
     *   <li>Sprawdza wynik końcowy gry</li>
     * </ol>
     */
    @Test
    public void testFullCleanupAndFillProcess() {
        forceStone(3, 0, Stone.BLACK);
        forceStone(3, 1, Stone.BLACK);
        forceStone(3, 2, Stone.BLACK);
        forceStone(2, 2, Stone.BLACK);
        forceStone(1, 2, Stone.BLACK);
        forceStone(0, 2, Stone.BLACK);
        forceStone(18, 18, Stone.BLACK);

        forceStone(0, 0, Stone.WHITE);
        forceStone(1, 0, Stone.WHITE);
        forceStone(16, 18, Stone.WHITE);
        forceStone(16, 17, Stone.WHITE);
        forceStone(16, 16, Stone.WHITE);
        forceStone(17, 16, Stone.WHITE);
        forceStone(18, 16, Stone.WHITE);
        assertEquals(GameState.IN_PROGRESS, game.getState(), "Gra powinna być w toku");

        game.pass();
        game.pass();

        if (game.getCurrentPlayer() != Stone.BLACK) {
            game.switchPlayer();
        }

        assertEquals(GameState.CLEANUP, game.getState(), "Po 2 pasach powinna zacząć się faza CLEANUP");

        String res1 = game.removeDeadStone(0, 0, Stone.BLACK);
        String res2 = game.removeDeadStone(1, 0, Stone.BLACK);
        assertEquals("OK", res1);
        assertEquals("OK", res2);
        assertEquals(2, game.getBlackPrisoners(), "Czarny powinien mieć 2 jeńców");
        assertTrue(game.getBoard().isEmpty(0, 0));

        game.switchPlayer();

        String res3 = game.removeDeadStone(18, 18, Stone.WHITE);
        assertEquals("OK", res3);
        assertEquals(1, game.getWhitePrisoners(), "Biały powinien mieć 1 jeńca");

        game.switchPlayer();
        Boolean fillRes = game.placePrisonerAsDead(18, 17, Stone.BLACK);
        assertEquals(true, fillRes);
        assertEquals(1, game.getBlackPrisoners(), "Czarny zużył jednego jeńca, został 1");
        assertEquals(Stone.WHITE, game.getBoard().get(18, 17), "Na 19 18 powinien teraz stać biały kamień");

        game.pass();
        game.pass();

        assertEquals(GameState.FINISHED, game.getState(), "Po kolejnych 2 pasach gra powinna się zakończyć");
        System.out.println("Wynik końcowy: " + game.getGameResult());
    }

    private void forceStone(int x, int y, Stone color) {
        game.getBoard().placeStone(x, y, color);
    }
}
