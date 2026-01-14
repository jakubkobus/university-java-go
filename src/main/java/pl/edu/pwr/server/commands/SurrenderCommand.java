package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;

/**
 * Komenda serwera do poddania się gracza.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Interpretowanie żądania klienta do poddania się (surrender)</li>
 *   <li>Rejestrację poddania się w stanie gry</li>
 *   <li>Wysyłanie informacji do obu graczy o wyniku gry</li>
 *   <li>Obsługę scenariusza, gdy gra jest już skończona</li>
 * </ul>
 * 
 * Komenda umożliwia graczowi zrezygnowanie z gry, co skutkuje jego porażką
 * i zwycięstwem przeciwnika. Gra jest natychmiast kończona.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 */
public class SurrenderCommand implements Command {
  
  /** Instancja gry, na której będą wykonywane operacje */
  private final Game game;

  /**
   * Konstruktor SurrenderCommand.
   * Inicjalizuje komendę z referencją do obiektu gry.
   * 
   * @param game instancja gry, na której będzie wykonywana komenda
   */
  public SurrenderCommand(Game game) {
    this.game = game;
  }

  /**
   * Egzekwuje komendę poddania się gracza.
   * 
   * Przepływ:
   * <ol>
   *   <li>Sprawdza czy gra już się nie skończyła</li>
   *   <li>Rejestruje poddanie się gracza w stanie gry</li>
   *   <li>Wysyła informację do gracza, że się poddał i przegrał</li>
   *   <li>Informuje przeciwnika o poddaniu się i wysyła wynik gry</li>
   * </ol>
   * 
   * @param args tablica argumentów (nieużywana dla tej komendy)
   * @param sender ClientHandler reprezentujący gracza, który wysłał komendę
   */
  @Override
  public void execute(String[] args, ClientHandler sender) {
    if(game.isGameOver()) return;
    
    game.surrender(sender.getMyColor());
    sender.sendMessage("GAME_OVER Poddales sie. Przegrales.");
    
    ClientHandler opponent = sender.getOpponent();
    if(opponent != null) {
      opponent.sendMessage("INFO Przeciwnik sie poddal.");
      opponent.sendMessage("GAME_OVER " + game.getGameResult());
    }
  }
}