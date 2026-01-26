package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.server.ServerMessages;
import pl.edu.pwr.database.service.GameService;

/**
 * Komenda serwera do wypełniania terytoriów jeńcami.
 * 
 * Odpowiada za:
 * <ul>
 * <li>Interpretowanie żądania klienta do umieszczenia jenca na terytoriach</li>
 * <li>Walidację współrzędnych i dostępności jeńców</li>
 * <li>Aktualizowanie stanu gry i wysyłanie potwierdzenia do obu graczy</li>
 * <li>Obsługę błędów (bledne współrzędne, brak jeńców)</li>
 * </ul>
 * 
 * Komenda jest używana podczas fazy czyszczenia (cleanup phase) do oznaczenia,
 * które terytoria są kontrolowane przez którego gracza.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 */
public class FillCommand extends BaseCommand {

  /**
   * Konstruktor FillCommand.
   * Inicjalizuje komendę z referencją do obiektu gry,
   * serwisem do zarządzania grami oraz identyfikatorem gry.
   * 
   * @param game        instancja gry, na której będzie wykonywana komenda
   * @param gameService serwis do zarządzania grami w bazie danych
   * @param gameId      identyfikator gry w bazie danych
   */
  public FillCommand(Game game, GameService gameService, Long gameId) {
    super(game, gameService, gameId);
  }

  /**
   * Konstruktor FillCommand.
   * Inicjalizuje komendę z referencją do obiektu gry.
   * 
   * @param game instancja gry, na której będzie wykonywana komenda
   */
  public FillCommand(Game game) {
    super(game);
  }

  /**
   * Egzekwuje komendę wypełnienia terytoriów jeńcami.
   * 
   * Przepływ:
   * <ol>
   * <li>Waliduje liczbę argumentów (musi być dokładnie 3)</li>
   * <li>Parsuje współrzędne x i y (konwertuje z 1-indeksowanych na
   * 0-indeksowane)</li>
   * <li>Próbuje umieścić jenca na podanym polu korzystając z koloru gracza</li>
   * <li>Jeśli się powiedzie, wysyła zaktualizowaną planszę obu graczom</li>
   * <li>Jeśli się nie powiedzie, wysyła komunikat błędu do gracza</li>
   * </ol>
   * 
   * @param args   tablica argumentów: [0] = "FILL", [1] = współrzędna x, [2] =
   *               współrzędna y
   * @param sender ClientHandler reprezentujący gracza, który wysłał komendę
   */
  @Override
  public void execute(String[] args, ClientHandler sender) {
    if (!validateArgCount(args, 3, sender, "FILL x y")) {
      return;
    }

    try {
      int[] coords = parseCoordinates(args);
      int x = coords[0];
      int y = coords[1];

      if (game.placePrisonerAsDead(x, y, sender.getMyColor())) {
        Stone filledColor = sender.getMyColor() == Stone.BLACK ? Stone.WHITE : Stone.BLACK;
        if (gameService != null && gameId != null) {
          gameService.saveMove(
              gameId,
              game.getMoveCount(),
              x,
              y,
              filledColor.toString(),
              "FILL");
        }

        notifyBothPlayers(
            sender,
            ServerMessages.INFO_PRISONER_PLACED,
            ServerMessages.INFO_OPPONENT_PLACED_PRISONER);
      } else {
        sender.sendMessage(ServerMessages.ERROR_CANNOT_PLACE_PRISONER);
      }
    } catch (NumberFormatException e) {
      sender.sendMessage(ServerMessages.ERROR_INVALID_COORDINATES);
    }
  }
}
