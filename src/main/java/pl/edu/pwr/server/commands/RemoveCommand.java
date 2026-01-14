package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.logic.GameState;

/**
 * Komenda serwera do usuwania martwych kamieni z planszy.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Interpretowanie żądania klienta do usunięcia martwego kamienia</li>
 *   <li>Walidację, że gra jest w fazie czyszczenia (CLEANUP)</li>
 *   <li>Walidację współrzędnych kamienia do usunięcia</li>
 *   <li>Aktualizowanie stanu gry i wysyłanie potwierdzenia do obu graczy</li>
 *   <li>Obsługę błędów (gra nie w fazie czyszczenia, brak kamienia)</li>
 * </ul>
 * 
 * Komenda jest używana podczas fazy czyszczenia (cleanup phase), po tym jak obaj gracze
 * pominęli turę. Gracze usuwają martwe kamienie przed ostatecznym liczeniem punktów.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 * @see GameState
 */
public class RemoveCommand implements Command {
    
    /** Instancja gry, na której będą wykonywane operacje */
    private final Game game;

    /**
     * Konstruktor RemoveCommand.
     * Inicjalizuje komendę z referencją do obiektu gry.
     * 
     * @param game instancja gry, na której będzie wykonywana komenda
     */
    public RemoveCommand(Game game) {
        this.game = game;
    }

    /**
     * Egzekwuje komendę usunięcia martwego kamienia.
     * 
     * Przepływ:
     * <ol>
     *   <li>Sprawdza czy gra jest w fazie czyszczenia (CLEANUP)</li>
     *   <li>Waliduje liczbę argumentów (musi być dokładnie 3)</li>
     *   <li>Parsuje współrzędne x i y (konwertuje z 1-indeksowanych na 0-indeksowane)</li>
     *   <li>Synchronicznie próbuje usunąć martwy kamień z planszy</li>
     *   <li>Jeśli usunięcie się powiedzie, wysyła zaktualizowaną planszę obu graczom</li>
     *   <li>Jeśli usunięcie się nie powiedzie, wysyła komunikat błędu do gracza</li>
     * </ol>
     * 
     * Operacja jest zsynchronizowana, aby zapobiec wyścigom danych w środowisku wielowątkowym.
     * 
     * @param args tablica argumentów: [0] = "REMOVE", [1] = współrzędna x, [2] = współrzędna y
     * @param sender ClientHandler reprezentujący gracza, który wysłał komendę
     */
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
