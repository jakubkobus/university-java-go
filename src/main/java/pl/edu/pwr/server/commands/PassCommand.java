package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.GameConstants;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.server.ServerMessages;
import pl.edu.pwr.database.service.GameService;

/**
 * Komenda serwera do pominięcia tury gracza.
 * 
 * Odpowiada za:
 * <ul>
 * <li>Interpretowanie żądania klienta do pominięcia tury (pass)</li>
 * <li>Walidację, że jest tura gracza wysyłającego komendę</li>
 * <li>Rejestrację pominięcia w stanie gry</li>
 * <li>Wysyłanie informacji do obu graczy o pominięciu tury</li>
 * <li>Sprawdzenie czy gra się skończyła (dwa pasy z rzędu)</li>
 * </ul>
 * 
 * Komenda obsługuje logikę zakończenia gry - jeśli obaj gracze pasują z rzędu,
 * gra przechodzi do fazy czyszczenia i liczenia punktów.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 */
public class PassCommand extends BaseCommand {

  /**
   * Konstruktor PassCommand.
   * Inicjalizuje komendę z referencją do obiektu gry,
   * serwisem do zarządzania grami oraz identyfikatorem gry.
   * 
   * @param game        instancja gry, na której będzie wykonywana komenda
   * @param gameService serwis do zarządzania grami w bazie danych
   * @param gameId      identyfikator gry w bazie danych
   */
  public PassCommand(Game game, GameService gameService, Long gameId) {
    super(game, gameService, gameId);
  }

  /**
   * Konstruktor PassCommand.
   * Inicjalizuje komendę z referencją do obiektu gry.
   * 
   * @param game instancja gry, na której będzie wykonywana komenda
   */
  public PassCommand(Game game) {
    super(game);
  }

  /**
   * Egzekwuje komendę pominięcia tury (pass).
   * 
   * Przepływ:
   * <ol>
   * <li>Sprawdza czy jest tura gracza wysyłającego komendę</li>
   * <li>Rejestruje pass w stanie gry (zmienia turę na przeciwnika)</li>
   * <li>Wysyła potwierdzenie do gracza, który spasował</li>
   * <li>Informuje przeciwnika o pominięciu tury</li>
   * <li>Jeśli gra się skończyła, wysyła wynik do obu graczy</li>
   * <li>W przeciwnym wypadku wysyła planszę do przeciwnika</li>
   * </ol>
   * 
   * @param args   tablica argumentów (nieużywana dla tej komendy)
   * @param sender ClientHandler reprezentujący gracza, który wysłał komendę
   */
  @Override
  public void execute(String[] args, ClientHandler sender) {
    int moveCount;
    boolean isGameOver;
    String gameResult;
    
    synchronized (game) {
      if (!validateTurn(sender)) {
        return;
      }

      game.pass();
      
      moveCount = game.getMoveCount();
      isGameOver = game.isGameOver();
      gameResult = game.getGameResult();
    }

    if (gameService != null && gameId != null) {
      gameService.saveMove(
        gameId,
        moveCount,
        GameConstants.PASS_COORDINATE,
        GameConstants.PASS_COORDINATE,
        sender.getMyColor().toString(),
        "PASS"
      );
    }

    ClientHandler opponent = sender.getOpponent();
    
    if (isGameOver) {
      notifyBothPlayers(sender, ServerMessages.INFO_PASSED, ServerMessages.INFO_OPPONENT_PASSED);
      sender.sendMessage("GAME_OVER " + gameResult);
      
      if (opponent != null) {
        opponent.sendMessage("GAME_OVER " + gameResult);
      }

      if (gameService != null) {
        gameService.saveGameResult(gameId, gameResult);
      }
    } else {
      notifyBothPlayers(sender, ServerMessages.INFO_PASSED, ServerMessages.INFO_OPPONENT_PASSED);
    }
  }
}