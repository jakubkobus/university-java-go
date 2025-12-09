package pl.edu.pwr.logic;

public class Board {

    private final int size;
    private final Stone[][] grid;

    public Board(int size) {
        this.size = size;
        this.grid = new Stone[size][size];

        // Init board with NONE
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = Stone.NONE;
            }
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean isEmpty(int x, int y) {
        return grid[x][y] == Stone.NONE;
    }

    public Stone get(int x, int y) {
        return grid[x][y];
    }

    public void placeStone(int x, int y, Stone stone) {
        grid[x][y] = stone;
    }
}
