package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;

public class SurrenderCommand implements Command {
  private final Game game;

  public SurrenderCommand(Game game) {
    this.game = game;
  }

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