package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.Stone;
import pl.edu.pwr.server.BotHandler;
import pl.edu.pwr.server.ClientHandler;
/**
 * Komenda służąca do rozpoczęcia gry z botem.
 * <p>
 * Po wywołaniu tej komendy:
 * <ul>
 *   <li>Ustawiana jest flaga gry z botem w obiekcie Game</li>
 *   <li>Tworzony jest obiekt BotHandler reprezentujący bota</li>
 *   <li>Gracz i bot zostają wzajemnie ustawieni jako przeciwnicy</li>
 *   <li>Bot uruchamiany jest w osobnym wątku</li>
 *   <li>Gracz otrzymuje komunikaty informacyjne oraz aktualny stan planszy</li>
 * </ul>
 * Jeśli w grze jest już dołączony inny gracz, komenda zwróci komunikat o błędzie.
 *
 * @author Jakub Kobus, Dawid Leśkiewicz
 */
public class BotCommand implements Command {
    /** Obiekt reprezentujący logikę gry, w której ma grać bot */
    private final Game game;
    /**
     * Konstruktor BotCommand.
     *
     * @param game obiekt gry, w której ma zostać dodany bot
     */
    public BotCommand(Game game) {
        this.game = game;
    }
    /**
     * Wykonuje komendę dodania bota do gry.
     * <p>
     * Jeśli w grze nie ma przeciwnika, tworzy nowego bota, ustawia go jako przeciwnika
     * dla gracza i uruchamia w osobnym wątku. Wysyła również komunikaty do klienta
     * gracza oraz aktualny stan planszy.
     * <p>
     * Jeśli przeciwnik już dołączył, wysyłany jest komunikat o błędzie.
     *
     * @param args tablica argumentów komendy (nieużywana w tej implementacji)
     * @param player obiekt ClientHandler reprezentujący gracza wykonującego komendę
     */
    @Override
    public synchronized void execute(String[] args, ClientHandler player) {
        if (player.getOpponent() != null) {
            player.sendMessage("ERR Ktos wlasnie dolaczyl! Nie mozna dodac bota.");
            player.sendMessage("OPPONENT_JOINED");
            player.sendBoard();
            return;
        }

        game.setBotGame(true);

        int botId = (player.getMyColor() == Stone.BLACK) ? 2 : 1;
        BotHandler bot = new BotHandler(game, botId);

        player.setOpponent(bot);
        bot.setOpponent(player);

        new Thread(bot).start();

        System.out.println("[SERWER] Gracz wybral bota.");
        player.sendMessage("INFO Gramy z botem.");
        player.sendMessage("OPPONENT_JOINED");
        player.sendBoard();
    }
}