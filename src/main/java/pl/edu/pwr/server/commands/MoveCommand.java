package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Board;
import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.MoveResult;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.server.ServerMessages;
import pl.edu.pwr.database.service.GameService;

import java.util.ArrayList;
import java.util.List;

/**
 * Komenda serwera do wykonywania ruchów w grze Go.
 * 
 * Odpowiada za:
 * <ul>
 * <li>Interpretowanie żądania klienta do umieszczenia kamienia</li>
 * <li>Walidację ruchu (sprawdzenie tur, granic planszy, samobójstwa)</li>
 * <li>Aktualizowanie stanu gry i wysyłanie potwierdzenia do obu graczy</li>
 * <li>Obsługę błędów (złe współrzędne, niedozwolony ruch)</li>
 * <li>Synchronizacja dostępu do gry w środowisku wielowątkowym</li>
 * </ul>
 * 
 * Komenda zapewnia, że tylko gracza mającego turę może wykonać ruch
 * i że ruch jest zgodny z regułami gry w Go.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 */
public class MoveCommand extends BaseCommand {

  /**
   * Konstruktor MoveCommand.
   * Inicjalizuje komendę z referencją do obiektu gry,
   * serwisem do zarządzania grami oraz identyfikatorem gry.
   * 
   * @param game        instancja gry, na której będą wykonywane ruchy
   * @param gameService serwis do zarządzania grami w bazie danych
   * @param gameId      identyfikator gry w bazie danych
   */
  public MoveCommand(Game game, GameService gameService, Long gameId) {
    super(game, gameService, gameId);
  }

  /**
   * Konstruktor MoveCommand.
   * Inicjalizuje komendę z referencją do obiektu gry.
   * 
   * @param game instancja gry, na której będą wykonywane ruchy
   */
  public MoveCommand(Game game) {
    super(game);
  }

  /**
   * Egzekwuje komendę ruchu (umieszczenia kamienia na planszy).
   * 
   * Przepływ:
   * <ol>
   * <li>Waliduje liczbę argumentów (musi być dokładnie 3)</li>
   * <li>Parsuje współrzędne x i y (konwertuje z 1-indeksowanych na
   * 0-indeksowane)</li>
   * <li>Sprawdza czy jest tura gracza wysyłającego komendę</li>
   * <li>Synchronicznie wykonuje ruch na grze</li>
   * <li>Jeśli ruch jest dozwolony, wysyła zaktualizowaną planszę obu graczom</li>
   * <li>Jeśli ruch jest niedozwolony, wysyła komunikat błędu do gracza</li>
   * </ol>
   * 
   * Operacja jest zsynchronizowana, aby zapobiec wyścigom danych w środowisku
   * wielowątkowym.
   * 
   * @param args   tablica argumentów: [0] = "MOVE", [1] = współrzędna x, [2] =
   *               współrzędna y
   * @param sender ClientHandler reprezentujący gracza, który wysłał komendę
   */
  @Override
  public void execute(String[] args, ClientHandler sender) {
    if (!validateArgCount(args, 3, sender, "MOVE x y")) {
      return;
    }

    try {
      int[] coords = parseCoordinates(args);
      int x = coords[0];
      int y = coords[1];

      Stone playerColor = sender.getMyColor();

      MoveResult result;
      int moveCount;
      List<Board.Point> capturedStones;
      
      synchronized (game) {
        if (!validateTurn(sender)) {
          return;
        }
        
        result = game.makeMove(x, y);
        
        if (result.success()) {
          moveCount = game.getMoveCount();
          capturedStones = game.getLastCapturedStones();
        } else {
          moveCount = 0;
          capturedStones = new ArrayList<>();
        }
      }

      if (!result.success()) {
        String errorMsg = switch (result.reason()) {
          case GAME_OVER -> ServerMessages.ERROR_GAME_OVER;
          case OUT_OF_BOUNDS -> ServerMessages.ERROR_OUT_OF_BOUNDS;
          case OCCUPIED -> ServerMessages.ERROR_OCCUPIED;
          case SUICIDE -> ServerMessages.ERROR_SUICIDE;
          case KO_RULE -> ServerMessages.ERROR_KO_RULE;
          default -> "ERR Ruch niedozwolony: " + result.errorMessage();
        };
        sender.sendMessage(errorMsg);
        return;
      }

      if (gameService != null && gameId != null) {
        gameService.saveMove(
          gameId,
          moveCount,
          x,
          y,
          playerColor.toString(),
          "MOVE"
        );
        
        Stone capturedColor = (playerColor == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
        for (var point : capturedStones) {
          gameService.saveMove(
            gameId,
            moveCount,
            point.x(),
            point.y(),
            capturedColor.toString(),
            "CAPTURE"
          );
        }
      }

      notifyBothPlayers(
        sender,
        ServerMessages.infoMoveExecuted(x + 1, y + 1),
        ServerMessages.infoOpponentMoved(x + 1, y + 1)
      );

    } catch (NumberFormatException e) {
      sender.sendMessage(ServerMessages.ERROR_INVALID_COORDINATES);
    }
  }
}