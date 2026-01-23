package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;
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
public class PassCommand implements Command {

  /** Instancja gry, na której będą wykonywane operacje */
  private final Game game;

  /** Serwis do zarządzania grami w bazie danych */
  private GameService gameService;

  /** Identyfikator gry w bazie danych */
  private Long gameId;

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
    this.game = game;
    this.gameService = gameService;
    this.gameId = gameId;
  }

  /**
   * Konstruktor PassCommand.
   * Inicjalizuje komendę z referencją do obiektu gry.
   * 
   * @param game instancja gry, na której będzie wykonywana komenda
   */
  public PassCommand(Game game) {
    this(game, null, null);
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
    if (game.getCurrentPlayer() != sender.getMyColor()) {
      sender.sendMessage("ERR To nie Twoja tura!");
      return;
    }

    game.pass();

    if (gameService != null && gameId != null) {
      gameService.saveMove(
        gameId,
        game.getMoveCount(),
        -1,
        -1,
        sender.getMyColor().toString(),
        "PASS"
      );
    }

    sender.sendMessage("INFO Spasowales");

    ClientHandler opponent = sender.getOpponent();
    if (opponent != null) {
      opponent.sendMessage("INFO Przeciwnik spasowal");
      if (game.isGameOver()) {
        sender.sendMessage("GAME_OVER " + game.getGameResult());
        opponent.sendMessage("GAME_OVER " + game.getGameResult());

        if (gameService != null) {
          gameService.saveGameResult(gameId, game.getGameResult());
        }
      } else {
        opponent.sendBoard();
      }
    } else if (game.isGameOver()) {
      sender.sendMessage("GAME_OVER " + game.getGameResult());
    }

    if (!game.isGameOver())
      sender.sendBoard();
  }
}