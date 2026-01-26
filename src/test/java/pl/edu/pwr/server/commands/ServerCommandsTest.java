package pl.edu.pwr.server.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.GameConstants;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.ClientHandler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Testy dla komend serwera w grze Go.
 * 
 * Testuje wykonywanie komend przesyłanych przez graczy:
 * <ul>
 *   <li>MOVE - umieszczenie kamienia na planszy</li>
 *   <li>PASS - pominięcie tury</li>
 *   <li>REMOVE - usunięcie martwego kamienia (faza cleanup)</li>
 *   <li>FILL - umieszczenie jeńca na terenie (faza cleanup)</li>
 *   <li>SURRENDER - poddanie się gracza</li>
 * </ul>
 * 
 * Testuje również walidację komend i komunikację między graczami.
 * Używa mocking framework (Mockito) do symulacji ClientHandler i Game.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see MoveCommand
 * @see RemoveCommand
 * @see SurrenderCommand
 */
class ServerCommandsTest {

  private Game game;
  private ClientHandler sender;
  private ClientHandler opponent;

  @BeforeEach
  void setUp() {
    game = Mockito.spy(new Game(GameConstants.STANDARD_BOARD_SIZE));
    sender = Mockito.mock(ClientHandler.class);
    opponent = Mockito.mock(ClientHandler.class);

    when(sender.getOpponent()).thenReturn(opponent);
    when(sender.getMyColor()).thenReturn(Stone.BLACK);
    when(opponent.getMyColor()).thenReturn(Stone.WHITE);
  }

  /**
   * Test prawidłowego wykonania komendy MOVE.
   * 
   * Sprawdza czy:
   * <ol>
   *   <li>Komenda MOVE wywoła metodę makeMove na grze</li>
   *   <li>Plansza zostanie wysłana obu graczom</li>
   *   <li>Obaj gracze otrzymają komunikat INFO</li>
   * </ol>
   */
  @Test
  void testMoveCommandValid() {
    MoveCommand cmd = new MoveCommand(game);
    String[] args = { "MOVE", "1", "1" };

    cmd.execute(args, sender);

    verify(game).makeMove(0, 0);
    verify(sender).sendBoard();
    verify(sender).sendMessage(contains("INFO"));
    verify(opponent).sendMessage(contains("INFO"));
  }

  /**
   * Test komendy MOVE gdy nie jest tura gracza.
   * 
   * Sprawdza czy system odrzuci ruch gracza, gdy nie jest jego tura,
   * i czy makeMove nie zostanie wezwany więcej niż raz.
   */
  @Test
  void testMoveCommandWrongTurn() {
    MoveCommand cmd = new MoveCommand(game);
    String[] args = { "MOVE", "1", "1" };

    game.makeMove(10, 10);

    cmd.execute(args, sender);

    verify(sender).sendMessage(contains("ERR"));
    verify(game, times(1)).makeMove(anyInt(), anyInt());
  }

  /**
   * Test komendy REMOVE (usunięcia martwego kamienia).
   * 
   * Sprawdza czy system odrzuci komendę REMOVE
   * gdy gra nie jest w stanie CLEANUP.
   */
  @Test
  void testRemoveCommandOnlyInCleanup() {
    RemoveCommand cmd = new RemoveCommand(game);
    String[] args = { "REMOVE", "1", "1" };

    cmd.execute(args, sender);

    verify(sender).sendMessage(contains("ERR"));
  }

  /**
   * Test komendy SURRENDER (poddania się gracza).
   * 
   * Sprawdza czy:
   * <ol>
   *   <li>Komenda SURRENDER kończy grę (isGameOver = true)</li>
   *   <li>Obaj gracze otrzymają komunikat GAME_OVER</li>
   * </ol>
   */
  @Test
  void testSurrenderCommand() {
    SurrenderCommand cmd = new SurrenderCommand(game);

    cmd.execute(new String[] { "SURRENDER" }, sender);

    assertTrue(game.isGameOver());
    verify(sender).sendMessage(contains("GAME_OVER"));
    verify(opponent).sendMessage(contains("GAME_OVER"));
  }
}