package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;

/**
 * Komenda serwera do wypełniania terytoriów jeńcami.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Interpretowanie żądania klienta do umieszczenia jenca na terytoriach</li>
 *   <li>Walidację współrzędnych i dostępności jeńców</li>
 *   <li>Aktualizowanie stanu gry i wysyłanie potwierdzenia do obu graczy</li>
 *   <li>Obsługę błędów (bledne współrzędne, brak jeńców)</li>
 * </ul>
 * 
 * Komenda jest używana podczas fazy czyszczenia (cleanup phase) do oznaczenia,
 * które terytoria są kontrolowane przez którego gracza.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 * @see Game
 */
public class FillCommand implements Command {
    
    /** Instancja gry, na której będą wykonywane operacje */
    private final Game game;

    /**
     * Konstruktor FillCommand.
     * Inicjalizuje komendę z referencją do obiektu gry.
     * 
     * @param game instancja gry, na której będzie wykonywana komenda
     */
    public FillCommand(Game game) {
        this.game = game;
    }

    /**
     * Egzekwuje komendę wypełnienia terytoriów jeńcami.
     * 
     * Przepływ:
     * <ol>
     *   <li>Waliduje liczbę argumentów (musi być dokładnie 3)</li>
     *   <li>Parsuje współrzędne x i y (konwertuje z 1-indeksowanych na 0-indeksowane)</li>
     *   <li>Próbuje umieścić jenca na podanym polu korzystając z koloru gracza</li>
     *   <li>Jeśli się powiedzie, wysyła zaktualizowaną planszę obu graczom</li>
     *   <li>Jeśli się nie powiedzie, wysyła komunikat błędu do gracza</li>
     * </ol>
     * 
     * @param args tablica argumentów: [0] = "FILL", [1] = współrzędna x, [2] = współrzędna y
     * @param sender ClientHandler reprezentujący gracza, który wysłał komendę
     */
    @Override
    public void execute(String[] args, ClientHandler sender) {
        if (args.length != 3) return;

        try {
            int x = Integer.parseInt(args[1]) - 1;
            int y = Integer.parseInt(args[2]) - 1;

            if (game.placePrisonerAsDead(x, y, sender.getMyColor())) {
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
