package pl.edu.pwr.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Obsługuje logikę zbijania kamieni w grze Go.
 * 
 * Odpowiada za:
 * <ul>
 * <li>Wykrywanie zbitych grup kamieni (grup bez liberties)</li>
 * <li>Usuwanie zbitych kamieni z planszy</li>
 * <li>Liczenie liberties grupy algorytmem DFS</li>
 * <li>Śledzenie zbitych kamieni dla historii gry</li>
 * </ul>
 * 
 * Klasa enkapsuluje złożoną logikę zbijania, czyniąc klasę Game
 * mniejszą i bardziej skoncentrowaną na zarządzaniu stanem gry.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Game
 * @see Board
 */
public class CaptureHandler {

  /**
   * Sprawdza i usuwa zbite grupy przeciwnika sąsiadujące z nowo postawionym
   * kamieniem.
   * 
   * @param board         plansza gry
   * @param x             współrzędna x nowo postawionego kamienia
   * @param y             współrzędna y nowo postawionego kamienia
   * @param opponentColor kolor przeciwnika, którego grupy należy sprawdzić
   * @return lista pozycji zbitych kamieni
   */
  public List<Board.Point> checkAndRemoveCaptures(Board board, int x, int y, Stone opponentColor) {
    List<Board.Point> capturedStones = new ArrayList<>();
    List<Board.Point> neighbors = board.getNeighbors(x, y);

    for (Board.Point neighbor : neighbors) {
      Stone neighborStone = board.get(neighbor.x(), neighbor.y());

      if (neighborStone == opponentColor) {
        if (countGroupLiberties(board, neighbor.x(), neighbor.y(), opponentColor) == 0) {
          List<Board.Point> removed = removeGroup(board, neighbor.x(), neighbor.y(), opponentColor);
          capturedStones.addAll(removed);
        }
      }
    }

    return capturedStones;
  }

  /**
   * Liczy liczbę liberties (pustych sąsiednich pozycji) dla grupy kamieni.
   * Używa algorytmu przeszukiwania w głąb (DFS) do eksploracji całej połączonej
   * grupy.
   * 
   * @param board  plansza gry
   * @param startX współrzędna x kamienia w grupie
   * @param startY współrzędna y kamienia w grupie
   * @param color  kolor grupy do sprawdzenia
   * @return liczba liberties (pustych pozycji sąsiadujących z grupą)
   */
  public int countGroupLiberties(Board board, int startX, int startY, Stone color) {
    Set<String> visited = new HashSet<>();
    Set<String> liberties = new HashSet<>();
    List<Board.Point> stack = new ArrayList<>();
    stack.add(new Board.Point(startX, startY));

    while (!stack.isEmpty()) {
      Board.Point current = stack.remove(0);
      String key = current.x() + "," + current.y();

      if (visited.contains(key)) {
        continue;
      }
      visited.add(key);

      for (Board.Point neighbor : board.getNeighbors(current.x(), current.y())) {
        Stone s = board.get(neighbor.x(), neighbor.y());
        if (s == Stone.NONE) {
          liberties.add(neighbor.x() + "," + neighbor.y());
        } else if (s == color) {
          stack.add(neighbor);
        }
      }
    }

    return liberties.size();
  }

  /**
   * Usuwa zbitą grupę kamieni z planszy.
   * Używa algorytmu przeszukiwania w głąb (DFS) do znalezienia wszystkich kamieni
   * w połączonej grupie.
   * 
   * @param board  plansza gry
   * @param startX współrzędna x kamienia w grupie
   * @param startY współrzędna y kamienia w grupie
   * @param color  kolor grupy do usunięcia
   * @return lista pozycji usuniętych kamieni
   */
  public List<Board.Point> removeGroup(Board board, int startX, int startY, Stone color) {
    List<Board.Point> stack = new ArrayList<>();
    stack.add(new Board.Point(startX, startY));
    Set<String> visited = new HashSet<>();
    List<Board.Point> removedStones = new ArrayList<>();

    while (!stack.isEmpty()) {
      Board.Point current = stack.remove(0);
      String key = current.x() + "," + current.y();

      if (visited.contains(key)) {
        continue;
      }
      visited.add(key);

      board.placeStone(current.x(), current.y(), Stone.NONE);
      removedStones.add(current);

      for (Board.Point neighbor : board.getNeighbors(current.x(), current.y())) {
        if (board.get(neighbor.x(), neighbor.y()) == color) {
          stack.add(neighbor);
        }
      }
    }

    return removedStones;
  }
}
