package pl.edu.pwr.server.commands;

import pl.edu.pwr.server.Server;
import pl.edu.pwr.server.ClientHandler;

/**
 * Klasa obsługująca komendę dołączenia do gry (JOIN).
 * Odpowiada za weryfikację stanu klienta i dodanie go do kolejki oczekujących na rozgrywkę PvP.
 */
public class JoinCommand implements Command {
    private final Server server;

    /**
     * Tworzy nową instancję komendy dołączania.
     *
     * @param server referencja do serwera, który zarządza kolejką graczy
     */
    public JoinCommand(Server server) {
        this.server = server;
    }

    /**
     * Wykonuje logikę dołączenia klienta do kolejki gier.
     * <p>
     * Najpierw sprawdza, czy klient nie uczestniczy już w aktywnej grze (wysyła błąd ERR, jeśli tak).
     * Następnie deleguje zadanie dodania do kolejki PvP do głównej klasy serwera.
     *
     * @param args   tablica argumentów komendy (w tym przypadku pusta/nieużywana)
     * @param sender obiekt obsługujący klienta, który wysłał komendę
     */
    @Override
    public void execute(String[] args, ClientHandler sender) {
        if (sender.getGame() != null) {
            sender.sendMessage("ERR Gra juz trwa!");
            return;
        }

        server.joinPvPQueue(sender);
    }
}