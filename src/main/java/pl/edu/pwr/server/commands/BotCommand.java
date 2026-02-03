package pl.edu.pwr.server.commands;

import pl.edu.pwr.database.service.GameService;
import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.GameConstants;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.server.ServerMessages;

/**
 * Klasa obsługująca komendę rozpoczęcia gry z komputerem (BOT).
 * Odpowiada za utworzenie nowej gry, rejestrację w bazie danych oraz
 * konfigurację klienta do trybu jednoosobowego.
 */
public class BotCommand implements Command {

    private final GameService gameService;

    /**
     * Tworzy nową instancję komendy uruchamiającej grę z botem.
     *
     * @param gameService serwis bazodanowy do zapisu informacji o nowej grze
     */
    public BotCommand(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Uruchamia logikę startu gry przeciwko botowi.
     * <p>
     * Proces wykonania:
     * <ol>
     * <li>Sprawdza, czy klient nie jest już w trakcie gry (blokuje, jeśli tak).</li>
     * <li>Tworzy nową instancję gry o standardowym rozmiarze.</li>
     * <li>Rejestruje grę w bazie danych (jeśli serwis jest dostępny), oznaczając graczy jako HUMAN vs BOT_CPU.</li>
     * <li>Przypisuje grę do klienta (gracz zawsze gra Czarnymi kamieniami - ID 1).</li>
     * <li>Aktywuje tryb bota w instancji ClientHandler.</li>
     * <li>Wysyła potwierdzenie rozpoczęcia oraz aktualny stan planszy do gracza.</li>
     * </ol>
     *
     * @param args   argumenty komendy (nieużywane w tym kontekście)
     * @param sender obiekt obsługujący klienta wysyłającego żądanie
     */
    @Override
    public void execute(String[] args, ClientHandler sender) {
        if (sender.getGame() != null) {
            sender.sendMessage("ERR Gra juz trwa!");
            return;
        }

        Game game = new Game(GameConstants.STANDARD_BOARD_SIZE);

        Long gameId = null;
        if (gameService != null) {
            gameId = gameService.startNewGame("HUMAN", "BOT_CPU");
        }

        sender.assignGame(game, gameId, 1);
        sender.enableBotMode();

        sender.sendMessage(ServerMessages.SERVER_BOT_STARTED);
        sender.sendBoard();
    }
}