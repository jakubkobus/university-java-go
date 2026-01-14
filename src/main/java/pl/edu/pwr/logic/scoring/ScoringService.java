package pl.edu.pwr.logic.scoring;

import pl.edu.pwr.logic.*;
import java.util.*;

public class ScoringService {
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

    private TerritoryScanResult scanEmptyArea(Board board, int x, int y, boolean[][] visited) {
        int size = board.getSize();
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

    public boolean isPrisonerRemovable(Board board, int x, int y, Stone playerColor) {
        Stone clickedStone = board.get(x, y);
        return (clickedStone != Stone.NONE && clickedStone != playerColor);
    }


    private record TerritoryScanResult(int areaSize, boolean touchesBlack, boolean touchesWhite) {}
}

