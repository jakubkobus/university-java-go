package pl.edu.pwr.server;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.logic.SimpleBot;
/**
 * Klasa reprezentująca bota w grze.
 * <p>
 * Bot działa jako wątek i podejmuje decyzje na podstawie logiki zawartej w {@link SimpleBot}.
 * Bot może być przeciwnikiem dla gracza ludzkiego i wykonuje ruchy automatycznie
 * w swojej turze.
 * <p>
 * Dziedziczy po {@link ClientHandler}, dzięki czemu może być używany w tej samej
 * infrastrukturze serwera co gracze ludzie.
 *
 * @author Jakub Kobus, Dawid Leśkiewicz
 */
public class BotHandler extends ClientHandler {
    /** Logika decyzji ruchów bota */
    private final SimpleBot botLogic;
    /**
     * Konstruktor BotHandler.
     *
     * @param game obiekt gry, w której bot ma grać
     * @param playerId identyfikator gracza (np. 1 lub 2) używany przez serwer
     */
    public BotHandler(Game game, int playerId) {
        super(null, playerId, game);
        this.botLogic = new SimpleBot();
    }
    /**
     * Główna pętla wątku bota.
     * <p>
     * Bot sprawdza czy gra się skończyła. Jeśli nie, w swojej turze podejmuje ruch
     * korzystając z logiki {@link SimpleBot}. Po wykonaniu ruchu lub spasowaniu
     * powiadamia przeciwnika o aktualnym stanie planszy.
     */
    @Override
    public void run() {
        System.out.println(">>> [BOT] Start wątku. Kolor ID: " + getMyColor() + " <<<");

        while (!game.isGameOver()) {
            try {
                boolean isMyTurn;
                synchronized (game) {
                    isMyTurn = (game.getCurrentPlayer() == getMyColor());
                }

                if (isMyTurn) {
                    System.out.println(">>> [BOT] MOJA TURA! Myślę... <<<");
                    Thread.sleep(1000);

                    executeBotTurn();
                } else {
                    Thread.sleep(500);
                }

            } catch (Exception e) {
                System.err.println("!!! [BOT] BŁĄD KRYTYCZNY W PĘTLI !!!");
                e.printStackTrace();
                break;
            }
        }
        System.out.println(">>> [BOT] Koniec gry. Wyłączam się. <<<");
    }
    /**
     * Wykonuje turę bota.
     * <p>
     * Bot próbuje wykonać ruch przy użyciu {@link SimpleBot#performMove(Game)}.
     * Jeśli nie ma możliwego ruchu, bot pasuje. Po ruchu lub spasowaniu powiadamiany
     * jest przeciwnik o stanie planszy.
     */
    private void executeBotTurn() {
        boolean moveMade;

        synchronized (game) {
            moveMade = botLogic.performMove(game);
        }

        if (moveMade) {
            System.out.println("[BOT] Wykonano ruch. Powiadamiam przeciwnika.");
        } else {
            System.out.println("[BOT] Spasowano. Powiadamiam przeciwnika.");
        }

        if (getOpponent() != null) {
            getOpponent().sendBoard();
        } else {
            System.out.println("!!! [BOT] Uwaga: Brak przeciwnika (getOpponent() == null) !!!");
        }
    }
    /**
     * Metoda wysyłająca wiadomość do bota.
     * <p>
     * Nie jest używana, ponieważ bot nie komunikuje się poprzez standardowy kanał wiadomości.
     *
     * @param message treść wiadomości
     */
    @Override
    public void sendMessage(String message) {}
    /**
     * Metoda wysyłająca aktualny stan planszy do bota.
     * <p>
     * Nie jest używana, ponieważ bot ma bezpośredni dostęp do obiektu {@link Game}.
     */
    @Override
    public void sendBoard() {}
}