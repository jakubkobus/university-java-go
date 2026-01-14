package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.logic.GameState;


public class RemoveCommand implements Command {
    private final Game game;

    public RemoveCommand(Game game) {
        this.game = game;
    }

    @Override
    public void execute(String[] args, ClientHandler sender) {
        if (game.getState() != GameState.CLEANUP) {
            sender.sendMessage("ERR Mozesz usuwac kamienie tylko po zakonczeniu fazy ruchow (2x PASS)");
            return;
        }

        if (args.length != 3) {
            sender.sendMessage("ERR Uzyj: REMOVE x y");
            return;
        }

        try {
            int x = Integer.parseInt(args[1]) - 1;
            int y = Integer.parseInt(args[2]) - 1;

            String success;
            synchronized (game) {
                success = game.removeDeadStone(x, y, sender.getMyColor());
            }

            if (success.equals("OK")) {
                sender.sendBoard();
                sender.sendMessage("INFO: Usunieto martwy kamien.");
                if (sender.getOpponent() != null) {
                    sender.getOpponent().sendBoard();
                    sender.getOpponent().sendMessage("INFO: Przeciwnik usunal martwy kamien.");
                }
            } else {
                sender.sendMessage("ERR Nie ma tam kamienia do usuniecia.");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("ERR Wspolrzedne musza byc liczbami.");
        }
    }
}
