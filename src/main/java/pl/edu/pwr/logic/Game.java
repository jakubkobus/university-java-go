package pl.edu.pwr.logic;

import pl.edu.pwr.logic.scoring.IScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringService;
import java.util.List;
import java.util.ArrayList;

/**
 * Główna klasa logiki gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Zarządzanie stanem gry (plansza, gracze, tury)</li>
 *   <li>Walidację i wykonywanie ruchów graczy</li>
 *   <li>Detekcję i obsługę zbytych kamieni (jeńców)</li>
 *   <li>Obsługę faz gry (gra aktywna, cleanup, koniec)</li>
 *   <li>Liczenie punktów i określenie zwycięzcy</li>
 *   <li>Synchronizację dostępu w środowisku wielowątkowym</li>
 * </ul>
 * 
 * Gra przechodzi przez następujące stany: IN_PROGRESS → CLEANUP → FINISHED.
 * W każdej fazie gracze mogą wykonywać różne akcje (ruchy, pass, remove dead stones).
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Board
 * @see GameState
 * @see IScoringStrategy
 */
public class Game {
  private final Board board;
  private Board previousBoard;
  private Stone currentPlayer = Stone.BLACK;

  private int blackPrisoners = 0;
  private int whitePrisoners = 0;

  private int passesInRow = 0;
  private boolean gameOver = false;
  private String gameResult = "";

  private int moveCount = 0;

  private GameState state = GameState.IN_PROGRESS;
  private final IScoringStrategy scoringStrategy = new ScoringStrategy();
  private final ScoringService scoringService = new ScoringService();
  private final CaptureHandler captureHandler = new CaptureHandler();
  
  private List<Board.Point> lastCapturedStones = new ArrayList<>();

  /**
   * Konstruktor Game.
   * Inicjalizuje grę z planszą o określonym rozmiarze.
   * 
   * @param size rozmiar planszy (9, 13 lub 19)
   */
  public Game(int size) {
    this.board = BoardFactory.createBoard(size);
  }

  /**
   * Wykonuje ruch (umieszczenie kamienia na planszy).
   * 
   * Przepływ:
   * <ol>
   *   <li>Sprawdza czy gra nie jest już skończona</li>
   *   <li>Waliduje czy pole jest w granicach i jest puste</li>
   *   <li>Umieszcza kamień gracza na planszy</li>
   *   <li>Sprawdza czy zbite zostały grupy przeciwnika</li>
   *   <li>Sprawdza czy ruch nie jest samobójcą (suicide move)</li>
   *   <li>Sprawdza Ko rule (niedozwolone powtórzenie pozycji)</li>
   *   <li>Zapisuje stan planszy i zmienia turę na drugiego gracza</li>
   * </ol>
   * 
   * Operacja jest zsynchronizowana dla bezpieczeństwa w środowisku wielowątkowym.
   * 
   * @param x współrzędna x pola (0-indeksowana)
   * @param y współrzędna y pola (0-indeksowana)
   * @return MoveResult zawierający informację o sukcesie lub szczegółowy błąd
   */
  public synchronized MoveResult makeMove(int x, int y) {
    if (gameOver)
      return MoveResult.ofError(MoveResult.MoveFailureReason.GAME_OVER, "Game is already over");

    if (!board.isWithinBounds(x, y))
      return MoveResult.ofError(MoveResult.MoveFailureReason.OUT_OF_BOUNDS, 
          String.format("Position (%d, %d) is outside the board boundaries", x, y));

    if (!board.isEmpty(x, y))
      return MoveResult.ofError(MoveResult.MoveFailureReason.OCCUPIED, 
          String.format("Position (%d, %d) is already occupied", x, y));

    Board tempBoard = new Board(board);
    tempBoard.placeStone(x, y, currentPlayer);

    Stone opponent = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;

    Board backupBoard = new Board(board);
    int backupBlack = blackPrisoners;
    int backupWhite = whitePrisoners;

    lastCapturedStones.clear();

    board.placeStone(x, y, currentPlayer);
    lastCapturedStones = captureHandler.checkAndRemoveCaptures(board, x, y, opponent);
    
    int capturedCount = lastCapturedStones.size();
    if (capturedCount > 0) {
      if (currentPlayer == Stone.BLACK) {
        blackPrisoners += capturedCount;
      } else {
        whitePrisoners += capturedCount;
      }
    }

    if (captureHandler.countGroupLiberties(board, x, y, currentPlayer) == 0) {
      restoreBoard(backupBoard, backupBlack, backupWhite);
      return MoveResult.ofError(MoveResult.MoveFailureReason.SUICIDE, 
          String.format("Move at (%d, %d) would be suicide - group has no liberties", x, y));
    }

    if (previousBoard != null && board.isTheSameAs(previousBoard)) {
      restoreBoard(backupBoard, backupBlack, backupWhite);
      return MoveResult.ofError(MoveResult.MoveFailureReason.KO_RULE, 
          String.format("Move at (%d, %d) violates Ko rule - would repeat previous board state", x, y));
    }

    moveCount++;
    passesInRow = 0;
    previousBoard = backupBoard;

    switchPlayer();
    return MoveResult.ofSuccess();
  }

  /**
   * Przywraca planszę do poprzedniego stanu.
   * Używana gdy ruch musi być cofnięty (samobójstwo, Ko rule, itp.)
   * 
   * @param backup plansza do przywrócenia
   * @param bPris liczba jeńców czarnych do przywrócenia
   * @param wPris liczba jeńców białych do przywrócenia
   */
  private void restoreBoard(Board backup, int bPris, int wPris) {
    for (int i = 0; i < board.getSize(); i++)
      for (int j = 0; j < board.getSize(); j++)
        board.placeStone(i, j, backup.get(i, j));

    this.blackPrisoners = bPris;
    this.whitePrisoners = wPris;
  }

  /**
   * Rejestruje pominięcie tury gracza (pass).
   * 
   * Jeśli obaj gracze spasują z rzędu (2 pasy), gra przechodzi do fazy CLEANUP.
   * Jeśli w fazie CLEANUP obaj spasują, gra się kończy (FINISHED).
   * 
   * Operacja jest zsynchronizowana dla bezpieczeństwa w środowisku wielowątkowym.
   */
  public synchronized void pass() {
    if (gameOver)
      return;
    moveCount++;
    passesInRow++;

    if (passesInRow >= 2) {
      if (state == GameState.IN_PROGRESS) {
        state = GameState.CLEANUP;
        passesInRow = 0;
      } else if (state == GameState.CLEANUP) {
        state = GameState.FINISHED;
        finishGame();
      }
    } else {
      switchPlayer();
    }
  }

  /**
   * Kończy grę poprzez poddanie się gracza.
   * Gracz tracący zostaje uznany za przegrywającego.
   * 
   * Operacja jest zsynchronizowana dla bezpieczeństwa w środowisku wielowątkowym.
   * 
   * @param who gracz, który się poddaje (BLACK lub WHITE)
   */
  public synchronized void surrender(Stone who) {
    gameOver = true;
    Stone winner = (who == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    gameResult = "Poddanie, wygrywa " + (winner == Stone.BLACK ? "CZARNY" : "BIALY");
  }

  /**
   * Kończy grę i oblicza wynik.
   * Używa strategii punktacji do określenia zwycięzcy.
   * 
   * Operacja jest zsynchronizowana dla bezpieczeństwa w środowisku wielowątkowym.
   */
  public synchronized void finishGame() {
    GameResult result = scoringStrategy.score(
        board,
        blackPrisoners,
        whitePrisoners);

    gameOver = true;
    gameResult = "Wygrywa " + result.winner() +
        " (" + result.blackScore() + " : " + result.whiteScore() + ")";
  }

  /**
   * Usuwa martwy kamień w fazie cleanup.
   * Kamień może być usunięty tylko jeśli należy do przeciwnika
   * i jest całkowicie otoczony terytorium gracza żądającego usunięcia.
   * 
   * Operacja jest zsynchronizowana dla bezpieczeństwa w środowisku wielowątkowym.
   * 
   * @param x współrzędna x kamienia do usunięcia
   * @param y współrzędna y kamienia do usunięcia
   * @param requester gracz żądający usunięcia kamienia
   * @return "OK" jeśli kamień został usunięty, komunikat błędu w przeciwnym razie
   */
  public synchronized String removeDeadStone(int x, int y, Stone requester) {
    if (state != GameState.CLEANUP)
      return "ERR Faza to nie CLEANUP";

    Stone stoneAtPos = board.get(x, y);
    if (stoneAtPos == Stone.NONE)
      return "ERR To pole jest juz puste";

    if (stoneAtPos == requester) {
      return "ERR Nie mozesz usunac wlasnego kamienia!";
    }

    if (!scoringService.isPrisonerRemovable(board, x, y, requester)) {
      return "ERR Ten kamien/grupa nie jest w pelni otoczona Twoim terytorium!";
    }

    if (stoneAtPos == Stone.WHITE) {
      blackPrisoners++;
    } else {
      whitePrisoners++;
    }

    board.placeStone(x, y, Stone.NONE);

    return "OK";
  }

  /**
   * Zwraca aktualny stan gry.
   * THREAD-SAFE: Synchronized to ensure consistent read.
   * 
   * @return aktualny GameState (IN_PROGRESS, CLEANUP lub FINISHED)
   */
  public synchronized GameState getState() {
    return state;
  }

  /**
   * Umieszcza jeńca na terenie przeciwnika w fazie cleanup.
   * Gracze mogą umieszczać jeńców do zaznaczenia, które terytoria kontrolują.
   * 
   * Operacja jest zsynchronizowana dla bezpieczeństwa w środowisku wielowątkowym.
   * 
   * @param x współrzędna x pola do umieszczenia jenca
   * @param y współrzędna y pola do umieszczenia jenca
   * @param requester gracz umieszczający jenca (BLACK lub WHITE)
   * @return true jeśli jeńiec został umieszczony, false w przypadku błędu
   */
  public synchronized Boolean placePrisonerAsDead(int x, int y, Stone requester) {
    if (state != GameState.CLEANUP)
      return false;
    if (!board.isEmpty(x, y))
      return false;

    if (requester == Stone.BLACK) {
      if (blackPrisoners > 0) {
        board.placeStone(x, y, Stone.WHITE);
        blackPrisoners--;
        return true;
      }
    } else {
      if (whitePrisoners > 0) {
        board.placeStone(x, y, Stone.BLACK);
        whitePrisoners--;
        return true;
      }
    }
    return false;
  }

  /**
   * Zwraca planszę gry.
   * THREAD-SAFE: Returns defensive copy to prevent external modification.
   * 
   * @return Board zawierająca aktualny stan gry (kopia defensywna)
   */
  public synchronized Board getBoard() {
    return new Board(board);
  }

  /**
   * Zwraca bezpośredni dostęp do planszy (tylko do testów).
   * Package-private - dostępny tylko w tym samym pakiecie dla celów testowych.
   * UWAGA: Zwraca referencję do oryginalnej planszy, nie kopię!
   * 
   * @return Board - oryginalna plansza (nie kopia)
   */
  synchronized Board getBoardForTesting() {
    return board;
  }

  /**
   * Zwraca gracza mającego aktualnie turę.
   * THREAD-SAFE: Synchronized to ensure consistent read.
   * 
   * @return Stone (BLACK lub WHITE)
   */
  public synchronized Stone getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Zwraca liczbę jeńców czarnych graczy.
   * THREAD-SAFE: Synchronized to ensure consistent read.
   * 
   * @return liczba zbytych kamieni czarnych
   */
  public synchronized int getBlackPrisoners() {
    return blackPrisoners;
  }

  /**
   * Zwraca liczbę jeńców białych graczy.
   * THREAD-SAFE: Synchronized to ensure consistent read.
   * 
   * @return liczba zbytych kamieni białych
   */
  public synchronized int getWhitePrisoners() {
    return whitePrisoners;
  }

  /**
   * Sprawdza czy gra się skończyła.
   * THREAD-SAFE: Synchronized to ensure consistent read.
   * 
   * @return true jeśli gra jest skończona, false w przeciwnym razie
   */
  public synchronized boolean isGameOver() {
    return gameOver;
  }

  /**
   * Zwraca tekstowy wynik końcowy gry.
   * THREAD-SAFE: Synchronized to ensure consistent read.
   * 
   * @return string zawierający informacje o zwycięzcy i punktacji
   */
  public synchronized String getGameResult() {
    return gameResult;
  }

  /**
   * Zmienia turę na drugiego gracza.
   * Przechodzi z BLACK na WHITE lub odwrotnie.
   * THREAD-SAFE: Synchronized to ensure atomic state change.
   */
  public synchronized void switchPlayer() {
    currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
  }

  /**
   * Zwraca liczbę wykonanych ruchów w grze.
   * THREAD-SAFE: Synchronized to ensure consistent read.
   * 
   * @return liczba ruchów
   */
  public synchronized int getMoveCount() {
    return moveCount;
  }

  /**
   * Zwraca listę kamieni zbytych podczas ostatniego ruchu.
   * THREAD-SAFE: Synchronized and returns defensive copy.
   * 
   * @return lista punktów reprezentujących zbyte kamienie (kopia defensywna)
   */
  public synchronized List<Board.Point> getLastCapturedStones() {
    return new ArrayList<>(lastCapturedStones);
  }
}