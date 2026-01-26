package pl.edu.pwr.server.commands;

import pl.edu.pwr.logic.Game;
import pl.edu.pwr.server.ClientHandler;
import pl.edu.pwr.server.ServerMessages;
import pl.edu.pwr.database.service.GameService;

/**
 * Abstrakcyjna klasa bazowa dla komend gry redukująca duplikację kodu.
 * 
 * Zapewnia:
 * <ul>
 * <li>Wspólne pola (game, gameService, gameId)</li>
 * <li>Standardowe konstruktory</li>
 * <li>Metody pomocnicze dla wspólnych operacji</li>
 * <li>Narzędzia do parsowania współrzędnych</li>
 * </ul>
 * 
 * Wszystkie klasy komend powinny rozszerzać tę klasę bazową, aby korzystać
 * ze współdzielonej funkcjonalności i zachować spójność.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Command
 */
public abstract class BaseCommand implements Command {

  /** Instancja gry, na której będą wykonywane operacje */
  protected final Game game;

  /** Serwis do zarządzania grami w bazie danych */
  protected final GameService gameService;

  /** Identyfikator gry w bazie danych */
  protected final Long gameId;

  /**
   * Konstruktor ze wszystkimi zależnościami.
   * 
   * @param game        instancja gry, na której będą wykonywane operacje
   * @param gameService serwis do zarządzania grami w bazie danych (może być null)
   * @param gameId      identyfikator gry w bazie danych (może być null)
   */
  protected BaseCommand(Game game, GameService gameService, Long gameId) {
    this.game = game;
    this.gameService = gameService;
    this.gameId = gameId;
  }

  /**
   * Konstruktor bez zależności bazy danych.
   * Używany w testach lub gdy baza danych nie jest wymagana.
   * 
   * @param game instancja gry, na której będą wykonywane operacje
   */
  protected BaseCommand(Game game) {
    this(game, null, null);
  }

  /**
   * Parsuje współrzędne z argumentów komendy.
   * Konwertuje z 1-indeksowanych (wejście użytkownika) na 0-indeksowane
   * (wewnętrzne).
   * 
   * @param args argumenty komendy gdzie args[1] to x a args[2] to y
   * @return tablica [x, y] w formacie 0-indeksowanym
   * @throws NumberFormatException          jeśli współrzędne nie są poprawnymi
   *                                        liczbami całkowitymi
   * @throws ArrayIndexOutOfBoundsException jeśli args nie ma wystarczającej
   *                                        liczby elementów
   */
  protected int[] parseCoordinates(String[] args) {
    int x = Integer.parseInt(args[1]) - 1;
    int y = Integer.parseInt(args[2]) - 1;
    return new int[] { x, y };
  }

  /**
   * Waliduje, że komenda ma oczekiwaną liczbę argumentów.
   * 
   * @param args     argumenty komendy
   * @param expected oczekiwana liczba argumentów
   * @param sender   klient do wysłania komunikatu błędu, jeśli walidacja się nie
   *                 powiedzie
   * @param usage    komunikat użycia do wyświetlenia w przypadku błędu
   * @return true jeśli walidacja się powiodła, false w przeciwnym razie
   */
  protected boolean validateArgCount(String[] args, int expected, ClientHandler sender, String usage) {
    if (args.length != expected) {
      sender.sendMessage(ServerMessages.errorWrongFormat(usage));
      return false;
    }
    return true;
  }

  /**
   * Powiadamia obu graczy wysyłając planszę i komunikaty.
   * 
   * @param sender          gracz, który zainicjował akcję
   * @param senderMessage   komunikat do wysłania do nadawcy
   * @param opponentMessage komunikat do wysłania do przeciwnika
   */
  protected void notifyBothPlayers(ClientHandler sender, String senderMessage, String opponentMessage) {
    sender.sendBoard();
    sender.sendMessage(senderMessage);

    ClientHandler opponent = sender.getOpponent();
    if (opponent != null) {
      opponent.sendBoard();
      opponent.sendMessage(opponentMessage);
    }
  }

  /**
   * Sprawdza czy jest tura nadawcy.
   * 
   * @param sender klient próbujący wykonać akcję
   * @return true jeśli jest tura nadawcy, false w przeciwnym razie
   */
  protected boolean isSenderTurn(ClientHandler sender) {
    return game.getCurrentPlayer() == sender.getMyColor();
  }

  /**
   * Waliduje turę i wysyła komunikat błędu, jeśli nie jest tura nadawcy.
   * 
   * @param sender klient próbujący wykonać akcję
   * @return true jeśli walidacja się powiodła, false w przeciwnym razie
   */
  protected boolean validateTurn(ClientHandler sender) {
    if (!isSenderTurn(sender)) {
      sender.sendMessage(ServerMessages.ERROR_NOT_YOUR_TURN);
      return false;
    }
    return true;
  }
}
