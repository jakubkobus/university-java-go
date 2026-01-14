package pl.edu.pwr.server.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.ClientHandler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class ServerCommandsTest {

  private Game game;
  private ClientHandler sender;
  private ClientHandler opponent;

  @BeforeEach
  void setUp() {
    game = Mockito.spy(new Game(19));
    sender = Mockito.mock(ClientHandler.class);
    opponent = Mockito.mock(ClientHandler.class);

    when(sender.getOpponent()).thenReturn(opponent);
    when(sender.getMyColor()).thenReturn(Stone.BLACK);
    when(opponent.getMyColor()).thenReturn(Stone.WHITE);
  }

  @Test
  void testMoveCommandValid() {
    // Given
    MoveCommand cmd = new MoveCommand(game);
    String[] args = { "MOVE", "1", "1" };

    // When
    cmd.execute(args, sender);

    // Then
    verify(game).makeMove(0, 0);
    verify(sender).sendBoard();
    verify(sender).sendMessage(contains("INFO"));
    verify(opponent).sendMessage(contains("INFO"));
  }

  @Test
  void testMoveCommandWrongTurn() {
    MoveCommand cmd = new MoveCommand(game);
    String[] args = { "MOVE", "1", "1" };

    game.makeMove(10, 10);

    cmd.execute(args, sender);

    verify(sender).sendMessage(contains("ERR"));
    verify(game, times(1)).makeMove(anyInt(), anyInt());
  }

  @Test
  void testRemoveCommandOnlyInCleanup() {
    RemoveCommand cmd = new RemoveCommand(game);
    String[] args = { "REMOVE", "1", "1" };

    cmd.execute(args, sender);

    verify(sender).sendMessage(contains("ERR"));
  }

  @Test
  void testSurrenderCommand() {
    SurrenderCommand cmd = new SurrenderCommand(game);

    cmd.execute(new String[] { "SURRENDER" }, sender);

    assertTrue(game.isGameOver());
    verify(sender).sendMessage(contains("GAME_OVER"));
    verify(opponent).sendMessage(contains("GAME_OVER"));
  }
}