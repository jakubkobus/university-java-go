package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.server.ServerMessages;
import pl.edu.pwr.logic.GameState;
import pl.edu.pwr.database.service.GameService;

/**
 * Komenda serwera do usuwania martwych kamieni z planszy.
 * 
 * Odpowiada za:
 * <ul>
 * <li>Interpretowanie żądania klienta do usunięcia martwego kamienia</li>
 * <li>Walidację, że gra jest w fazie czyszczenia (CLEANUP)</li>
 * <li>Walidację współrzędnych kamienia do usunięcia</li>
 * <li>Aktualizowanie stanu gry i wysyłanie potwierdzenia do obu graczy</li>
 * <li>Obsługę błędów (gra nie w fazie czyszczenia, brak kamienia)</li>
 * </ul>
 * 
 * Komenda jest używana podczas fazy czyszczenia (cleanup phase), po tym jak
 * obaj gracze
 * pominęli turę. Gracze usuwają martwe kamienie przed ostatecznym liczeniem
 * punktów.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 * @see GameState
 */
public class RemoveCommand extends BaseCommand {

  /**
   * Konstruktor RemoveCommand.
   * Inicjalizuje komendę z referencją do obiektu gry,
   * serwisem do zarządzania grami oraz identyfikatorem gry.
   * 
   * @param game        instancja gry, na której będzie wykonywana komenda
   * @param gameService serwis do zarządzania grami w bazie danych
   * @param gameId      identyfikator gry w bazie danych
   */
  public RemoveCommand(Game game, GameService gameService, Long gameId) {
    super(game, gameService, gameId);
  }

  /**
   * Konstruktor RemoveCommand.
   * Inicjalizuje komendę z referencją do obiektu gry.
   * 
   * @param game instancja gry, na której będzie wykonywana komenda
   */
  public RemoveCommand(Game game) {
    super(game);
  }

  /**
   * Egzekwuje komendę usunięcia martwego kamienia.
   * 
   * Przepływ:
   * <ol>
   * <li>Sprawdza czy gra jest w fazie czyszczenia (CLEANUP)</li>
   * <li>Waliduje liczbę argumentów (musi być dokładnie 3)</li>
   * <li>Parsuje współrzędne x i y (konwertuje z 1-indeksowanych na
   * 0-indeksowane)</li>
   * <li>Synchronicznie próbuje usunąć martwy kamień z planszy</li>
   * <li>Jeśli usunięcie się powiedzie, wysyła zaktualizowaną planszę obu
   * graczom</li>
   * <li>Jeśli usunięcie się nie powiedzie, wysyła komunikat błędu do gracza</li>
   * </ol>
   * 
   * Operacja jest zsynchronizowana, aby zapobiec wyścigom danych w środowisku
   * wielowątkowym.
   * 
   * @param args   tablica argumentów: [0] = "REMOVE", [1] = współrzędna x, [2] =
   *               współrzędna y
   * @param sender ClientHandler reprezentujący gracza, który wysłał komendę
   */
  @Override
  public void execute(String[] args, ClientHandler sender) {
    if (game.getState() != GameState.CLEANUP) {
      sender.sendMessage(ServerMessages.ERROR_NOT_CLEANUP);
      return;
    }

    if (!validateArgCount(args, 3, sender, "REMOVE x y")) {
      return;
    }

    try {
      int[] coords = parseCoordinates(args);
      int x = coords[0];
      int y = coords[1];

      Stone removedColor = game.getBoard().get(x, y);

      String success;
      synchronized (game) {
        success = game.removeDeadStone(x, y, sender.getMyColor());
      }

      if (success.equals("OK")) {
        if (gameService != null && gameId != null && removedColor != Stone.NONE) {
          gameService.saveMove(
              gameId,
              game.getMoveCount(),
              x,
              y,
              removedColor.toString(),
              "REMOVE");
        }

        notifyBothPlayers(
            sender,
            ServerMessages.INFO_DEAD_STONE_REMOVED,
            ServerMessages.INFO_OPPONENT_REMOVED_DEAD);
      } else {
        sender.sendMessage(ServerMessages.ERROR_NO_STONE);
      }
    } catch (NumberFormatException e) {
      sender.sendMessage(ServerMessages.ERROR_INVALID_COORDINATES);
    }
  }
}
