package pl.edu.pwr.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Klasa implementująca prostego bota do gry w Go.
 * <p>
 * Bot podejmuje decyzje na podstawie heurystycznej oceny planszy,
 * biorąc pod uwagę bezpieczeństwo grup, sąsiedztwo przeciwnika
 * oraz unikanie ruchów samobójczych.
 */
public class SimpleBot {

    private final Random random = new Random();

    /**
     * Główna metoda wykonująca ruch bota.
     * <p>
     * Algorytm działania:
     * <ol>
     * <li>Przeszukuje całą planszę w poszukiwaniu legalnych ruchów.</li>
     * <li>Filtruje ruchy: pomija zajęte pola, własne bezpieczne terytorium oraz ruchy samobójcze.</li>
     * <li>Ocenia każde pole za pomocą funkcji heurystycznej {@link #evaluatePosition}.</li>
     * <li>Wybiera listę najlepszych ruchów (o najwyższym wyniku).</li>
     * <li>Losuje jeden ruch z listy najlepszych, aby uniknąć przewidywalności.</li>
     * <li>Próbuje wykonać ruch na obiekcie gry. Jeśli ruch jest niedozwolony (np. KO), ponawia próbę z kolejnym najlepszym ruchem.</li>
     * </ol>
     * Jeśli nie znajdzie żadnego sensownego ruchu lub wszystkie są nielegalne, bot pasuje.
     *
     * @param game aktualny stan gry, na którym bot ma wykonać ruch.
     * @return {@code true} jeśli bot postawił kamień, {@code false} jeśli bot spasował.
     */
    public boolean performMove(Game game) {
        int size = game.getBoard().getSize();
        Stone myColor = game.getCurrentPlayer();
        Stone opponentColor = (myColor == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;

        List<Point> bestMoves = new ArrayList<>();
        int maxScore = Integer.MIN_VALUE;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                if (game.getBoard().get(x, y) != Stone.NONE) continue;

                if (!isSafeTerritory(game.getBoard(), x, y, myColor)) continue;

                if (getLibertiesAfterMove(game.getBoard(), x, y, myColor) == 0) continue;

                int score = evaluatePosition(game.getBoard(), x, y, myColor, opponentColor);

                if (score > maxScore) {
                    maxScore = score;
                    bestMoves.clear();
                    bestMoves.add(new Point(x, y));
                } else if (score == maxScore) {
                    bestMoves.add(new Point(x, y));
                }
            }
        }

        if (bestMoves.isEmpty() || maxScore < -50) {
            game.pass();
            return false;
        }

        while (!bestMoves.isEmpty()) {
            int index = random.nextInt(bestMoves.size());
            Point chosen = bestMoves.get(index);

            MoveResult result = game.makeMove(chosen.x, chosen.y);

            if (result.success()) {
                return true;
            } else {
                bestMoves.remove(index);
            }
        }

        game.pass();
        return false;
    }

    /**
     * Ocenia wartość strategiczną danego pola dla bota.
     * <p>
     * Strategia oceny:
     * <ul>
     * <li>Premiuje sąsiadów w pionie i poziomie (tworzenie silnych połączeń).</li>
     * <li>Dodaje punkty za bliską obecność przeciwnika (atak/blokada).</li>
     * <li>Kary za tworzenie "ciężkich" kształtów (zbyt gęste upakowanie własnych kamieni).</li>
     * <li>Lekki bonus za zajmowanie krawędzi planszy.</li>
     * <li>Dodaje niewielki element losowy, aby urozmaicić grę.</li>
     * </ul>
     *
     * @param board    aktualny stan planszy.
     * @param x        współrzędna X ocenianego pola.
     * @param y        współrzędna Y ocenianego pola.
     * @param myColor  kolor kamieni bota.
     * @param oppColor kolor kamieni przeciwnika.
     * @return liczba całkowita reprezentująca ocenę ruchu (im wyższa, tym lepsza).
     */
    private int evaluatePosition(Board board, int x, int y, Stone myColor, Stone oppColor) {
        int score = 0;

        int myHoriz = 0;
        int myVert = 0;
        int oppNeighbors = 0;

        if (board.isWithinBounds(x - 1, y) && board.get(x - 1, y) == myColor) myHoriz++;
        if (board.isWithinBounds(x + 1, y) && board.get(x + 1, y) == myColor) myHoriz++;
        if (board.isWithinBounds(x, y - 1) && board.get(x, y - 1) == myColor) myVert++;
        if (board.isWithinBounds(x, y + 1) && board.get(x, y + 1) == myColor) myVert++;

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
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
        if (x == 0 || x == size - 1) score += 1;
        if (y == 0 || y == size - 1) score += 1;

        score += new Random().nextInt(3);

        return score;
    }

    /**
     * Symuluje postawienie kamienia i sprawdza liczbę oddechów nowo powstałej grupy.
     * Używane głównie do wykrywania ruchów samobójczych.
     * <p>
     * <b>Wymagania:</b> Klasa {@code Board} musi posiadać konstruktor kopiujący.
     *
     * @param board plansza przed ruchem.
     * @param x     współrzędna X ruchu.
     * @param y     współrzędna Y ruchu.
     * @param color kolor stawianego kamienia.
     * @return liczba oddechów grupy po wykonaniu ruchu.
     */
    private int getLibertiesAfterMove(Board board, int x, int y, Stone color) {
        Board temp = new Board(board);
        temp.placeStone(x, y, color);
        return countLiberties(temp, x, y, color);
    }

    /**
     * Inicjuje proces rekurencyjnego liczenia oddechów dla grupy kamieni.
     *
     * @param board  aktualna plansza.
     * @param startX współrzędna X jednego z kamieni w grupie.
     * @param startY współrzędna Y jednego z kamieni w grupie.
     * @param color  kolor grupy kamieni.
     * @return liczba wolnych przecięć (oddechów) sąsiadujących z grupą.
     */
    private int countLiberties(Board board, int startX, int startY, Stone color) {
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        return countLibertiesRecursive(board, startX, startY, color, visited);
    }

    /**
     * Rekurencyjna metoda pomocnicza do zliczania oddechów.
     * Przechodzi przez połączone kamienie tego samego koloru i sumuje unikalne puste sąsiedztwa.
     *
     * @param board   plansza gry.
     * @param x       bieżąca współrzędna X.
     * @param y       bieżąca współrzędna Y.
     * @param color   szukany kolor kamieni.
     * @param visited tablica odwiedzonych pól (zapobiega pętlom).
     * @return 1 jeśli pole jest puste (oddech), 0 w przeciwnym razie (lub suma rekurencyjna).
     */
    private int countLibertiesRecursive(Board board, int x, int y, Stone color, boolean[][] visited) {
        if (!board.isWithinBounds(x, y)) return 0;
        if (visited[x][y]) return 0;
        visited[x][y] = true;
        Stone s = board.get(x, y);
        if (s == Stone.NONE) return 1;
        if (s != color) return 0;

        int sum = 0;
        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] d : dirs) sum += countLibertiesRecursive(board, x + d[0], y + d[1], color, visited);
        return sum;
    }

    /**
     * Sprawdza heurystycznie, czy dane pole należy do bezpiecznego terytorium gracza.
     * Zapobiega niepotrzebnemu wypełnianiu własnych "oczu" lub pewnego terytorium.
     *
     * @param board   plansza gry.
     * @param x       współrzędna X.
     * @param y       współrzędna Y.
     * @param myColor kolor gracza.
     * @return {@code true} jeśli pole jest uznane za bezpieczne (nie należy tam stawiać),
     * {@code false} jeśli warto rozważyć ruch w tym miejscu.
     */
    private boolean isSafeTerritory(Board board, int x, int y, Stone myColor) {
        int myNeighbors = 0;
        int oppNeighbors = 0;
        for (Board.Point n : board.getNeighbors(x, y)) {
            Stone s = board.get(n.x(), n.y());
            if (s == myColor) myNeighbors++;
            else if (s == Stone.BLACK) oppNeighbors++; // Zakładamy sprawdzenie obecności wroga
        }

        return myNeighbors <= 0 || oppNeighbors != 0;
    }

    /**
     * Pomocnicza klasa wewnętrzna reprezentująca punkt na planszy.
     */
    private static class Point {
        int x, y;

        /**
         * Tworzy nowy punkt.
         * @param x współrzędna pozioma.
         * @param y współrzędna pionowa.
         */
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}