package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.database.service.GameService;

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
public class MoveCommand implements Command {

  /** Instancja gry, na której będą wykonywane ruchy */
  private final Game game;

  /** Serwis do zarządzania grami w bazie danych */
  private GameService gameService;

  /** Identyfikator gry w bazie danych */
  private Long gameId;

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
    this.game = game;
    this.gameService = gameService;
    this.gameId = gameId;
  }

  /**
   * Konstruktor MoveCommand.
   * Inicjalizuje komendę z referencją do obiektu gry.
   * 
   * @param game instancja gry, na której będą wykonywane ruchy
   */
  public MoveCommand(Game game) {
    this(game, null, null);
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
    if (args.length != 3) {
      sender.sendMessage("ERR Zly format. Uzyj: MOVE x y");
      return;
    }

    try {
      int x = Integer.parseInt(args[1]) - 1;
      int y = Integer.parseInt(args[2]) - 1;

      Stone playerColor = sender.getMyColor();

      if (game.getCurrentPlayer() != playerColor) {
        sender.sendMessage("ERR To nie Twoja tura!");
        return;
      }

      boolean ok;
      synchronized (game) {
        ok = game.makeMove(x, y);
      }

      if (!ok) {
        sender.sendMessage("ERR Ruch niedozwolony (zajete, samobojstwo lub poza plansza)");
        return;
      }

      if (gameService != null && gameId != null) {
        gameService.saveMove(
          gameId,
          game.getMoveCount(),
          x,
          y,
          playerColor.toString(),
          "MOVE"
        );
      }

      sender.sendBoard();
      sender.sendMessage("INFO: Wykonano ruch: " + (x + 1) + " " + (y + 1));

      ClientHandler opponent = sender.getOpponent();
      if (opponent != null) {
        opponent.sendBoard();
        opponent.sendMessage("INFO: Przeciwnik wykonal ruch: " + (x + 1) + " " + (y + 1));
      }

    } catch (NumberFormatException e) {
      sender.sendMessage("ERR Wspolrzedne musza byc liczbami");
    }
  }
}