package pl.edu.pwr.logic.scoring;

import pl.edu.pwr.logic.*;
import java.util.*;

/**
 * Serwis do obliczania terytoriów i jeńców w grze Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Identyfikację terytoriów kontrolowanych przez każdego gracza</li>
 *   <li>Skanowanie pustych obszarów na planszy</li>
 *   <li>Określanie czy kamień może być usunięty jako jeńiec</li>
 * </ul>
 * 
 * Używa algorytmu przeszukiwania wszerz (BFS - Breadth-First Search)
 * do identyfikacji połączonych obszarów pustych pól.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see TerritoryResult
 * @see IScoringStrategy
 */
public class ScoringService {
  
  /**
   * Oblicza terytoria kontrolowane przez czarnych i białych graczy.
   * 
   * Algorytm:
   * <ol>
   *   <li>Iteruje po każdym polu planszy</li>
   *   <li>Dla każdego nieodwiedzonego pustego pola skanuje połączony obszar</li>
   *   <li>Obszar należy do gracza, jeśli jest otoczony tylko jego kamieniami</li>
   *   <li>Obszar nie należy do nikogo, jeśli graniczy z kamieniami obu graczy</li>
   * </ol>
   * 
   * @param board plansza gry
   * @return wynik zawierający liczbę pól terytorialnych dla każdego gracza
   * @see TerritoryResult
   */
  public TerritoryResult calculateTerritory(Board board) {
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];

        int black = 0;
        int white = 0;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                if (board.get(x, y) == Stone.NONE && !visited[x][y]) {
                    TerritoryScanResult result = scanEmptyArea(board, x, y, visited);

                    if (result.touchesBlack && !result.touchesWhite)
                        black += result.areaSize;
                    else if (result.touchesWhite && !result.touchesBlack)
                        white += result.areaSize;
                }
            }
        }
        return new TerritoryResult(black, white);
    }

    /**
     * Skanuje połączony obszar pustych pól za pomocą algorytmu BFS.
     * 
     * Metoda przeszukuje wszystkie połączone pola osiągalne z punktu startowego,
     * które są puste. Śledzi czy obszar graniczy z kamieniami czarnymi, białymi lub oboma.
     * 
     * Algorytm:
     * <ol>
     *   <li>Dodaje pole startowe do kolejki i oznacza jako odwiedzone</li>
     *   <li>Dla każdego pola w kolejce sprawdza sąsiednie pola</li>
     *   <li>Puste nieodwiedzone pola są dodawane do kolejki</li>
     *   <li>Kamienie są zaznaczane jako granice terytoriału</li>
     * </ol>
     * 
     * @param board plansza gry
     * @param x współrzędna x pola startowego
     * @param y współrzędna y pola startowego
     * @param visited tablica śledząca już odwiedzone pola
     * @return wynik skanowania zawierający rozmiar obszaru i jego granice
     */
    private TerritoryScanResult scanEmptyArea(Board board, int x, int y, boolean[][] visited) {
        Queue<Board.Point> queue = new LinkedList<>();
        queue.add(new Board.Point(x, y));
        visited[x][y] = true;

        int area = 0;
        boolean touchesBlack = false;
        boolean touchesWhite = false;

        while (!queue.isEmpty()) {
            Board.Point p = queue.poll();
            area++;

            for (Board.Point n : board.getNeighbors(p.x(), p.y())) {
                Stone s = board.get(n.x(), n.y());

                if (s == Stone.NONE && !visited[n.x()][n.y()]) {
                    visited[n.x()][n.y()] = true;
                    queue.add(n);
                } else if (s == Stone.BLACK) {
                    touchesBlack = true;
                } else if (s == Stone.WHITE) {
                    touchesWhite = true;
                }
            }
        }
        return new TerritoryScanResult(area, touchesBlack, touchesWhite);
    }

    /**
     * Sprawdza czy kamień może być usunięty jako jeńiec w fazie cleanup.
     * 
     * Kamień może być usunięty jeśli:
     * <ul>
     *   <li>Pole nie jest puste (zawiera kamień)</li>
     *   <li>Kamień nie należy do gracza żądającego usunięcia (jest jeńcem)</li>
     * </ul>
     * 
     * @param board plansza gry
     * @param x współrzędna x kamienia
     * @param y współrzędna y kamienia
     * @param playerColor kolor gracza chcącego usunąć kamień
     * @return true jeśli kamień może być usunięty, false w przeciwnym razie
     */
    public boolean isPrisonerRemovable(Board board, int x, int y, Stone playerColor) {
        Stone clickedStone = board.get(x, y);
        return (clickedStone != Stone.NONE && clickedStone != playerColor);
    }


    /**
     * Zagnieżdżony record zawierający wynik skanowania obszaru pustych pól.
     * 
     * Przechowuje informacje o:
     * <ul>
     *   <li>Rozmiarze (liczbie pól) skanowanego obszaru</li>
     *   <li>Czy obszar graniczy z czarnymi kamieniami</li>
     *   <li>Czy obszar graniczy z białymi kamieniami</li>
     * </ul>
     */
    private record TerritoryScanResult(int areaSize, boolean touchesBlack, boolean touchesWhite) {}
}

