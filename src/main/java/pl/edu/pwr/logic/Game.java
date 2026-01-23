package pl.edu.pwr.logic;

import pl.edu.pwr.logic.scoring.IScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringStrategy;
import pl.edu.pwr.logic.scoring.ScoringService;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

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
   * @return true jeśli ruch był dozwolony i wykonany, false w przeciwnym razie
   */
  public synchronized boolean makeMove(int x, int y) {
    if (gameOver)
      return false;

    if (!board.isWithinBounds(x, y) || !board.isEmpty(x, y))
      return false;

    Board tempBoard = new Board(board);
    tempBoard.placeStone(x, y, currentPlayer);

    Stone opponent = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;

    Board backupBoard = new Board(board);
    int backupBlack = blackPrisoners;
    int backupWhite = whitePrisoners;

    board.placeStone(x, y, currentPlayer);
    checkCaptures(x, y, opponent);

    if (countGroupLiberties(x, y, currentPlayer) == 0) {
      restoreBoard(backupBoard, backupBlack, backupWhite);
      return false;
    }

    if (previousBoard != null && board.isTheSameAs(previousBoard)) {
      restoreBoard(backupBoard, backupBlack, backupWhite);
      return false;
    }

    moveCount++;
    passesInRow = 0;
    previousBoard = backupBoard;

    switchPlayer();
    return true;
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
   * Sprawdza i usuwa zbyte grupy kamieni przeciwnika.
   * Metoda wywoływana po umieszczeniu kamienia.
   * 
   * @param x współrzędna x nowo umieszczonego kamienia
   * @param y współrzędna y nowo umieszczonego kamienia
   * @param opponentColor kolor przeciwnika (grupy do sprawdzenia)
   */
  private void checkCaptures(int x, int y, Stone opponentColor) {
    List<Board.Point> neighbors = board.getNeighbors(x, y);

    for (Board.Point n : neighbors) {
      Stone neighborStone = board.get(n.x(), n.y());

      if (neighborStone == opponentColor)
        if (countGroupLiberties(n.x(), n.y(), opponentColor) == 0)
          removeGroup(n.x(), n.y(), opponentColor);
    }
  }

  /**
   * Liczy wolne pola (liberties) dla grupy kamieni.
   * Używa algorytmu DFS (Depth-First Search) do eksploracji grupy.
   * 
   * @param startX współrzędna x pola startowego
   * @param startY współrzędna y pola startowego
   * @param color kolor grupy do sprawdzenia
   * @return liczba wolnych pól otaczających grupę
   */
  private int countGroupLiberties(int startX, int startY, Stone color) {
    Set<String> visited = new HashSet<>();
    Set<String> liberties = new HashSet<>();
    List<Board.Point> stack = new ArrayList<>();
    stack.add(new Board.Point(startX, startY));

    while (!stack.isEmpty()) {
      Board.Point current = stack.remove(0);
      String key = current.x() + "," + current.y();

      if (visited.contains(key))
        continue;
      visited.add(key);

      for (Board.Point neighbor : board.getNeighbors(current.x(), current.y())) {
        Stone s = board.get(neighbor.x(), neighbor.y());
        if (s == Stone.NONE)
          liberties.add(neighbor.x() + "," + neighbor.y());
        else if (s == color)
          stack.add(neighbor);
      }
    }
    return liberties.size();
  }

  /**
   * Usuwa zbytą grupę kamieni i dodaje je do jeńców aktualnego gracza.
   * Używa algorytmu DFS do znalezienia wszystkich kamieni w grupie.
   * 
   * @param startX współrzędna x kamienia należącego do grupy
   * @param startY współrzędna y kamienia należącego do grupy
   * @param color kolor grupy do usunięcia
   */
  private void removeGroup(int startX, int startY, Stone color) {
    List<Board.Point> stack = new ArrayList<>();
    stack.add(new Board.Point(startX, startY));
    Set<String> visited = new HashSet<>();

    int stonesCaptured = 0;

    while (!stack.isEmpty()) {
      Board.Point current = stack.remove(0);
      String key = current.x() + "," + current.y();

      if (visited.contains(key))
        continue;
      visited.add(key);

      board.placeStone(current.x(), current.y(), Stone.NONE);
      stonesCaptured++;

      for (Board.Point neighbor : board.getNeighbors(current.x(), current.y()))
        if (board.get(neighbor.x(), neighbor.y()) == color)
          stack.add(neighbor);
    }

    if (currentPlayer == Stone.BLACK) {
      blackPrisoners += stonesCaptured;
    } else {
      whitePrisoners += stonesCaptured;
    }
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
   * 
   * @return aktualny GameState (IN_PROGRESS, CLEANUP lub FINISHED)
   */
  public GameState getState() {
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
   * 
   * @return Board zawierająca aktualny stan gry
   */
  public Board getBoard() {
    return board;
  }

  /**
   * Zwraca gracza mającego aktualnie turę.
   * 
   * @return Stone (BLACK lub WHITE)
   */
  public Stone getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Zwraca liczbę jeńców czarnych graczy.
   * 
   * @return liczba zbytych kamieni czarnych
   */
  public int getBlackPrisoners() {
    return blackPrisoners;
  }

  /**
   * Zwraca liczbę jeńców białych graczy.
   * 
   * @return liczba zbytych kamieni białych
   */
  public int getWhitePrisoners() {
    return whitePrisoners;
  }

  /**
   * Sprawdza czy gra się skończyła.
   * 
   * @return true jeśli gra jest skończona, false w przeciwnym razie
   */
  public boolean isGameOver() {
    return gameOver;
  }

  /**
   * Zwraca tekstowy wynik końcowy gry.
   * 
   * @return string zawierający informacje o zwycięzcy i punktacji
   */
  public String getGameResult() {
    return gameResult;
  }

  /**
   * Zmienia turę na drugiego gracza.
   * Przechodzi z BLACK na WHITE lub odwrotnie.
   */
  public void switchPlayer() {
    currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
  }

  /**
   * Zwraca liczbę wykonanych ruchów w grze.
   * 
   * @return liczba ruchów
   */
  public int getMoveCount() {
    return moveCount;
  }
}