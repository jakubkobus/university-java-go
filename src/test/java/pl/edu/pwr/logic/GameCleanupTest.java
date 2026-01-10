package pl.edu.pwr.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameCleanupTest {
    private Game game;

    @BeforeEach
    public void setUp() {
        // Inicjalizacja gry na planszy 19x19
        game = new Game(19);
    }

    @Test
    public void testFullCleanupAndFillProcess() {
        // 1. Ustawienie kamieni (używamy współrzędnych 0-based w kodzie)
        // Czarne kamienie otaczające róg (1,1)
        forceStone(3, 0, Stone.BLACK); // 4 1
        forceStone(3, 1, Stone.BLACK); // 4 2
        forceStone(3, 2, Stone.BLACK); // 4 3
        forceStone(2, 2, Stone.BLACK); // 3 3
        forceStone(1, 2, Stone.BLACK); // 2 3
        forceStone(0, 2, Stone.BLACK); // 1 3
        forceStone(18, 18, Stone.BLACK); // 19 19 (martwy w rogu bialego)

        // Białe kamienie
        forceStone(0, 0, Stone.WHITE); // 1 1 (martwy w rogu czarnego)
        forceStone(1, 0, Stone.WHITE); // 2 1 (martwy w rogu czarnego)
        // Bialy mur otaczający róg (19,19)
        forceStone(16, 18, Stone.WHITE); // 17 19
        forceStone(16, 17, Stone.WHITE); // 17 18
        forceStone(16, 16, Stone.WHITE); // 17 17
        forceStone(17, 16, Stone.WHITE); // 18 17
        forceStone(18, 16, Stone.WHITE); // 19 17

        assertEquals(GameState.IN_PROGRESS, game.getState(), "Gra powinna być w toku");

        // 2. Obaj gracze pasują
        game.pass(); // Czarny pasuje
        game.pass(); // Biały pasuje

        if (game.getCurrentPlayer() != Stone.BLACK) {
            game.switchPlayer(); // Jeśli zrobiłeś ją publiczną
        }

        assertEquals(GameState.CLEANUP, game.getState(), "Po 2 pasach powinna zacząć się faza CLEANUP");

        // 3. Usuwanie martwych kamieni
        // Czarny usuwa białe kamienie ze swojego terytorium (1,1 i 2,1)
        // Zakładamy, że teraz jest tura białego (po 2 pasie), więc musimy upewnić się,
        // kto wykonuje akcję w Twoim ClientHandlerze.
        // Tutaj symulujemy akcje poprzez metodę removeDeadStone.

        String res1 = game.removeDeadStone(0, 0); // usuwa 1 1
        String res2 = game.removeDeadStone(1, 0); // usuwa 2 1
        assertEquals("OK", res1);
        assertEquals("OK", res2);
        assertEquals(2, game.getBlackPrisoners(), "Czarny powinien mieć 2 jeńców");
        assertTrue(game.getBoard().isEmpty(0, 0));

        // Zmiana tury na Białego, aby mógł usunąć czarny kamień (jeśli Twoja logika tego wymaga)
        game.switchPlayer();

        String res3 = game.removeDeadStone(18, 18); // usuwa 19 19
        assertEquals("OK", res3);
        assertEquals(1, game.getWhitePrisoners(), "Biały powinien mieć 1 jeńca");

        // 4. Wstawianie jeńców (FILL)
        // Czarny wstawia białego jeńca na puste pole w terytorium białego
        game.switchPlayer(); // Tura czarnego
        Boolean fillRes = game.placePrisonerAsDead(18, 17); // Stawia na 19 18
        assertEquals(true, fillRes);
        assertEquals(1, game.getBlackPrisoners(), "Czarny zużył jednego jeńca, został 1");
        assertEquals(Stone.WHITE, game.getBoard().get(18, 17), "Na 19 18 powinien teraz stać biały kamień");

        // 5. Zakończenie fazy CLEANUP
        game.pass(); // Czarny kończy usuwanie/wypełnianie
        game.pass(); // Biały kończy usuwanie/wypełnianie

        assertEquals(GameState.FINISHED, game.getState(), "Po kolejnych 2 pasach gra powinna się zakończyć");
        System.out.println("Wynik końcowy: " + game.getGameResult());
    }

    // Metoda pomocnicza do ustawiania kamieni z pominięciem logiki tur (tylko do testów)
    private void forceStone(int x, int y, Stone color) {
        game.getBoard().placeStone(x, y, color);
    }
}
