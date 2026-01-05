package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;

public class PassCommand implements Command {
  private final Game game;

  public PassCommand(Game game) {
    this.game = game;
  }

  @Override
  public void execute(String[] args, ClientHandler sender) {
    if(game.getCurrentPlayer() != sender.getMyColor()) {
      sender.sendMessage("ERR To nie Twoja tura!");
      return;
    }

    game.pass();
    sender.sendMessage("INFO Spasowales");
    
    ClientHandler opponent = sender.getOpponent();
    if(opponent != null) {
      opponent.sendMessage("INFO Przeciwnik spasowal");
      if(game.isGameOver()) {
        sender.sendMessage("GAME_OVER " + game.getGameResult());
        opponent.sendMessage("GAME_OVER " + game.getGameResult());
      } else {
        opponent.sendBoard(); 
      }
    } else if (game.isGameOver()) {
      sender.sendMessage("GAME_OVER " + game.getGameResult());
    }
    
    if(!game.isGameOver()) sender.sendBoard(); 
  }
}