package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;

public class FillCommand implements Command {
    private final Game game;

    public FillCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(String[] args, ClientHandler sender) {
        if (args.length != 3) return;

        try {
            int x = Integer.parseInt(args[1]) - 1;
            int y = Integer.parseInt(args[2]) - 1;

            if (game.placePrisonerAsDead(x, y)) {
                sender.sendBoard();
                sender.getOpponent().sendBoard();
                sender.sendMessage("INFO: Postawiono jenca na terytorium przeciwnika.");
            } else {
                sender.sendMessage("ERR Nie mozesz tu postawic jenca (brak jencow lub pole zajete).");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("ERR Bledne wspolrzedne.");
        }
    }
}
