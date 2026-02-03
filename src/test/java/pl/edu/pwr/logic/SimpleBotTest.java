package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Klasa testowa weryfikująca logikę i zachowanie prostego bota (SimpleBot).
 * Sprawdza poprawność ruchów, unikanie samobójstw oraz ogólną mechanikę tury.
 */
class SimpleBotTest {

    private Game game;
    private SimpleBot bot;
    private int boardSize = 9;

    /**
     * Przygotowuje środowisko testowe przed każdym testem.
     * Inicjalizuje nową grę o zadanym rozmiarze oraz instancję bota.
     */
    @BeforeEach
    void setUp() {
        game = new Game(boardSize);
        bot = new SimpleBot();
    }

    /**
     * Weryfikuje, czy bot potrafi wykonać poprawny ruch na całkowicie pustej planszy.
     * Sprawdza również, czy po ruchu tura poprawnie przechodzi na gracza BIAŁEGO.
     */
    @Test
    void testBotMakesValidMoveOnEmptyBoard() {
        boolean moved = bot.performMove(game);

        assertTrue(moved, "Bot powinien wykonać ruch na pustej planszy");
        assertEquals(Stone.WHITE, game.getCurrentPlayer(), "Po ruchu czarnego, tura powinna przejść do białego");
    }

    /**
     * Testuje mechanizm unikania ruchów samobójczych przez bota.
     * <p>
     * Scenariusz:
     * Tworzymy sytuację, w której róg (0,0) jest otoczony przez Białe kamienie:
     * <pre>
     * W .
     * . .
     * </pre>
     * (W kodzie ustawiamy kamienie na pozycjach (0,1) i (1,0)).
     * Bot (Czarny) nie powinien postawić kamienia na (0,0), ponieważ skutkowałoby to natychmiastowym przejęciem.
     */
    @Test
    void testBotAvoidsSuicide() {
        game.getBoard().set(0, 1, Stone.WHITE);
        game.getBoard().set(1, 0, Stone.WHITE);

        boolean moved = bot.performMove(game);

        assertTrue(moved, "Bot powinien znaleźć inny ruch niż samobójstwo");
        assertNotEquals(Stone.BLACK, game.getBoard().get(0, 0), "Bot nie powinien popełnić samobójstwa na (0,0)");
    }

    /**
     * Prosty test integracyjny sprawdzający, czy bot finalnie wykonuje ruch (zwraca true).
     * Weryfikuje ogólną stabilność metody performMove, nawet jeśli pierwszy wylosowany ruch byłby niepoprawny
     * (choć w tym prostym przypadku na pustej planszy każdy ruch jest poprawny).
     */
    @Test
    void testBotRetriesAfterInvalidMove() {
        boolean result = bot.performMove(game);
        assertTrue(result);
    }
}