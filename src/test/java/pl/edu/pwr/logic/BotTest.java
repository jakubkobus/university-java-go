package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pl.edu.pwr.logic.scoring.*;
/**
 * Testy jednostkowe dla zachowania gry z botem i usuwania martwych kamieni.
 * <p>
 * Testy sprawdzają:
 * <ul>
 *     <li>Tryb PVP i PVE</li>
 *     <li>Możliwość usuwania własnych i przeciwnych kamieni w fazie CLEANUP</li>
 *     <li>Zachowanie metody {@link Game#removeDeadStone(int, int, Stone)}</li>
 * </ul>
 */
public class BotTest {
    Game game;
    @BeforeEach
    void setup() {
        game = new Game(9);
    }
    /**
     * Test, że gracz w trybie PVE może usunąć kamień bota.
     */
    @Test
    void pvePlayerCanRemoveBotStone() {
        game.setBotGame(true);

        game.pass();
        game.pass();

        game.getBoard().placeStone(4, 4, Stone.WHITE);

        String result = game.removeDeadStone(4, 4, Stone.BLACK);

        assertEquals("OK", result);
    }
    /**
     * Test, że w trybie PVP gracz nie może usunąć własnego kamienia.
     */
    @Test
    void pvpPlayerCannotRemoveOwnStone() {
        game.setBotGame(false);

        game.pass();
        game.pass();

        game.getBoard().placeStone(3, 3, Stone.BLACK);

        String result = game.removeDeadStone(3, 3, Stone.BLACK);

        assertTrue(result.startsWith("ERR"));
    }
    /**
     * Test, że w trybie PVE gracz może usunąć własny kamień
     * (symulacja wykonania pracy bota za niego w CLEANUP).
     */
    @Test
    void pvePlayerCanRemoveOwnStone() {
        game.setBotGame(true);

        game.pass();
        game.pass();

        game.getBoard().placeStone(2, 2, Stone.BLACK);

        String result = game.removeDeadStone(2, 2, Stone.BLACK);

        assertEquals("OK", result);
    }
    /**
     * Test sprawdzający, czy bot w trybie PVE wykonuje legalny ruch.
     *
     * Scenariusz:
     * - Gra jest ustawiona na tryb PVE (bot przeciwnikiem).
     * - Bot wykonuje ruch na planszy.
     *
     * Oczekiwane zachowanie:
     * - Bot wykonuje ruch (true) **lub**
     * - Bot spasował, co zmienia aktualnego gracza na przeciwnika.
     */
    @Test
    void botMakesValidMove() {
        game.setBotGame(true);
        Stone botColor = game.getCurrentPlayer();

        SimpleBot bot = new SimpleBot();
        boolean moved = bot.performMove(game);

        assertTrue(moved || game.getCurrentPlayer() != botColor);
    }
    /**
     * Test sprawdzający, czy bot unika stawiania kamieni w swoim już otoczonym terytorium.
     *
     * Scenariusz:
     * - Gra jest ustawiona na tryb PVE.
     * - Tworzymy fragment planszy, gdzie bot ma własne terytorium (3x3).
     * - Bot wykonuje ruch.
     *
     * Oczekiwane zachowanie:
     * - Bot nie stawia kamienia w pełni otoczonym przez siebie terytorium.
     * - Może spaować, jeśli nie ma innych sensownych pól.
     */
    @Test
    void botAvoidsOwnTerritory() {
        game.setBotGame(true);

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                game.getBoard().placeStone(i, j, game.getCurrentPlayer());

        SimpleBot bot = new SimpleBot();
        boolean moved = bot.performMove(game);

        assertTrue(!moved);
    }




}
