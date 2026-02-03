package pl.edu.pwr.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * Prosty bot do gry w Go.
 * <p>
 * Bot wybiera ruchy na podstawie:
 * <ul>
 *   <li>Liczby sąsiadujących własnych kamieni (faworyzuje tworzenie "słupów")</li>
 *   <li>Obecności kamieni przeciwnika w pobliżu (atak/blokada)</li>
 *   <li>Unikania własnego terytorium już otoczonego</li>
 *   <li>Unikania samobójczych ruchów</li>
 * </ul>
 * <p>
 * Bot działa w kontekście jednej gry {@link Game} i zakłada, że jest
 * wywoływany tylko w swojej turze.
 *
 * @author Jakub Kobus, Dawid Leśkiewicz
 */
public class SimpleBot {
    /** Generator losowy do wyboru ruchów przy równych ocenach */
    private final Random random = new Random();
    /**
     * Wykonuje ruch w grze.
     * <p>
     * Bot przegląda wszystkie pola planszy i ocenia je według strategii:
     * - unika stawiania kamieni w swoim otoczonym terytorium
     * - unika ruchów samobójczych
     * - faworyzuje pozycje z większą liczbą własnych sąsiadów (słupy)
     * - atakuje/otacza przeciwnika
     * <p>
     * Jeśli nie ma sensownego ruchu, bot pasuje.
     *
     * @param game obiekt gry, w której bot ma wykonać ruch
     * @return true jeśli bot wykonał ruch, false jeśli spasował
     */
    public boolean performMove(Game game) {
        int size = game.getBoard().getSize();
        Stone myColor = game.getCurrentPlayer();
        Stone opponentColor = (myColor == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;

        List<Point> bestMoves = new ArrayList<>();
        int maxScore = Integer.MIN_VALUE;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                if (game.getBoard().get(x, y) != Stone.NONE)
                    continue;

                if (!isSafeTerritory(game.getBoard(), x, y, myColor)) {
                    continue;
                }


                if (getLibertiesAfterMove(game.getBoard(), x, y, myColor) == 0)
                    continue;

                int score = evaluatePosition(game.getBoard(), x, y, myColor, opponentColor);

                score += getLibertiesAfterMove(game.getBoard(), x, y, myColor);

                if (score > maxScore) {
                    maxScore = score;
                    bestMoves.clear();
                    bestMoves.add(new Point(x, y));
                } else if (score == maxScore) {
                    bestMoves.add(new Point(x, y));
                }
            }
        }

        if (bestMoves.isEmpty() || maxScore <= 0) {
            game.pass();
            return false;
        }

        Point chosen = bestMoves.get(random.nextInt(bestMoves.size()));
        boolean ok = game.makeMove(chosen.x, chosen.y);
        if (!ok) game.pass();
        return ok;
    }
    /**
     * Ocena pola dla bota.
     * <p>
     * Strategia:
     * - Więcej punktów za sąsiadów w pionie i poziomie (słupy zamiast grubych bloków)
     * - Premia za obecność przeciwnika w sąsiedztwie (atak/blokada)
     * - Kara za tworzenie pełnego narożnika własnych kamieni
     * - Lekki bonus za pola przy krawędzi
     * - Niewielka losowość
     *
     * @param board plansza gry
     * @param x współrzędna x
     * @param y współrzędna y
     * @param myColor kolor bota
     * @param oppColor kolor przeciwnika
     * @return ocena pola
     */
    private int evaluatePosition(Board board, int x, int y, Stone myColor, Stone oppColor) {
        int score = 0;

        int myHoriz = 0;
        int myVert = 0;
        int oppNeighbors = 0;

        if (board.isWithinBounds(x-1, y) && board.get(x-1, y) == myColor) myHoriz++;
        if (board.isWithinBounds(x+1, y) && board.get(x+1, y) == myColor) myHoriz++;
        if (board.isWithinBounds(x, y-1) && board.get(x, y-1) == myColor) myVert++;
        if (board.isWithinBounds(x, y+1) && board.get(x, y+1) == myColor) myVert++;

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int[] d : dirs) {
            int nx = x + d[0], ny = y + d[1];
            if (!board.isWithinBounds(nx, ny)) continue;
            if (board.get(nx, ny) == oppColor) oppNeighbors++;
        }

        score += (myHoriz + myVert) * 5;

        score += oppNeighbors * 3;

        if (myHoriz > 0 && myVert > 0) {
            score -= 10;
        }

        int size = board.getSize();
        if (x == 0 || x == size-1) score += 1;
        if (y == 0 || y == size-1) score += 1;

        score += new Random().nextInt(3);

        return score;
    }

    /**
     * Symulacja liczby oddechów po postawieniu kamienia.
     */
    private int getLibertiesAfterMove(Board board, int x, int y, Stone color) {
        Board temp = new Board(board);
        temp.placeStone(x, y, color);
        return countLiberties(temp, x, y, color);
    }

    private int countLiberties(Board board, int startX, int startY, Stone color) {
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        return countLibertiesRecursive(board, startX, startY, color, visited);
    }

    private int countLibertiesRecursive(Board board, int x, int y, Stone color, boolean[][] visited) {
        if (!board.isWithinBounds(x, y)) return 0;
        if (visited[x][y]) return 0;
        visited[x][y] = true;
        Stone s = board.get(x, y);
        if (s == Stone.NONE) return 1;
        if (s != color) return 0;

        int sum = 0;
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int[] d : dirs) sum += countLibertiesRecursive(board, x+d[0], y+d[1], color, visited);
        return sum;
    }
    /** Prosta klasa pomocnicza reprezentująca punkt na planszy */
    private static class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }
    /**
     * Sprawdza, czy pole nie jest już otoczonym terytorium własnym.
     * <p>
     * Bot unika stawiania w miejscu, które jest całkowicie otoczone przez własne kamienie,
     * aby nie tracić efektywności w tworzeniu nowych słupów/terytoriów.
     *
     * @param board plansza gry
     * @param x współrzędna x
     * @param y współrzędna y
     * @param myColor kolor bota
     * @return true jeśli pole jest bezpieczne do postawienia kamienia, false jeśli pole jest już w otoczonym terytorium
     */
    private boolean isSafeTerritory(Board board, int x, int y, Stone myColor) {
        int myNeighbors = 0;
        int oppNeighbors = 0;
        for (Board.Point n : board.getNeighbors(x, y)) {
            Stone s = board.get(n.x(), n.y());
            if (s == myColor) myNeighbors++;
            else if (s == Stone.BLACK) oppNeighbors++;
        }

        if (myNeighbors > 0 && oppNeighbors == 0) return false;

        return true;
    }



}
