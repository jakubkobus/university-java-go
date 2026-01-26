package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.server.ServerMessages;
import pl.edu.pwr.database.service.GameService;

/**
 * Komenda serwera do poddania się gracza.
 * 
 * Kończy grę poprzez poddanie się gracza, który wysłał komendę.
 * Przeciwnik automatycznie zostaje zwycięzcą.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 */
public class SurrenderCommand extends BaseCommand {

  /**
   * Konstruktor SurrenderCommand.
   * 
   * @param game        instancja gry
   * @param gameService serwis do zarządzania grami w bazie danych
   * @param gameId      identyfikator gry w bazie danych
   */
  public SurrenderCommand(Game game, GameService gameService, Long gameId) {
    super(game, gameService, gameId);
  }

  /**
   * Konstruktor SurrenderCommand bez serwisu bazy danych.
   * 
   * @param game instancja gry
   */
  public SurrenderCommand(Game game) {
    super(game);
  }

  /**
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
    
    sender.sendBoard();
    sender.sendMessage(ServerMessages.INFO_SURRENDERED);
    sender.sendMessage("GAME_OVER " + ServerMessages.INFO_SURRENDERED);
    
    ClientHandler opponent = sender.getOpponent();
    if(opponent != null) {
      opponent.sendBoard();
      opponent.sendMessage(ServerMessages.INFO_OPPONENT_SURRENDERED);
      opponent.sendMessage("GAME_OVER " + game.getGameResult());
    }
    
    if (gameService != null && gameId != null) {
      gameService.saveGameResult(gameId, game.getGameResult());
    }
  }
}