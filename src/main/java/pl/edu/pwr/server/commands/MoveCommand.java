package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.ClientHandler;

public class MoveCommand implements Command {
  private final Game game;

  public MoveCommand(Game game) {
    this.game = game;
  }

  @Override
  public void execute(String[] args, ClientHandler sender) {
    if(args.length != 3) {
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

      if(!ok) {
        sender.sendMessage("ERR Ruch niedozwolony (zajete, samobojstwo lub poza plansza)");
        return;
      }

      sender.sendBoard();
      sender.sendMessage("INFO: Wykonano ruch: " + (x + 1) + " " + (y + 1));

      ClientHandler opponent = sender.getOpponent();
      if(opponent != null) {
        opponent.sendBoard();
        opponent.sendMessage("INFO: Przeciwnik wykonal ruch: " + (x + 1) + " " + (y + 1));
      }

    } catch(NumberFormatException e) {
      sender.sendMessage("ERR Wspolrzedne musza byc liczbami");
    }
  }
}