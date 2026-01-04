package pl.edu.pwr.logic;

import java.util.ArrayList;
import java.util.List;

public class Board {
  private final int size;
  private final Stone[][] grid;

  public Board(int size) {
    this.size = size;
    this.grid = new Stone[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        grid[i][j] = Stone.NONE;
      }
    }
  }

  public Board(Board other) {
    this.size = other.size;
    this.grid = new Stone[size][size];
    for(int i = 0; i < size; i++)
      System.arraycopy(other.grid[i], 0, this.grid[i], 0, size);
  }

  public List<Point> getNeighbors(int x, int y) {
    List<Point> neighbors = new ArrayList<>();
    int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    for (int[] d : directions) {
      int nx = x + d[0];
      int ny = y + d[1];
      if (isWithinBounds(nx, ny)) {
        neighbors.add(new Point(nx, ny));
      }
    }
    return neighbors;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("  "); 
    for(int i = 0; i < size; i++)
      sb.append(String.format("%3d", i + 1));
    sb.append("\n");

    for(int y = 0; y < size; y++) {
      sb.append(String.format("%2d ", y + 1));
      for(int x = 0; x < size; x++) {
        switch (grid[x][y]) {
          case BLACK -> sb.append("(B)");
          case WHITE -> sb.append("(W)");
          case NONE -> sb.append(" + ");
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public boolean isTheSameAs(Board other) {
    if(other == null)
      return false;

    for(int i = 0; i < size; i++)
      for(int j = 0; j < size; j++)
        if(this.grid[i][j] != other.grid[i][j])
          return false;

    return true;
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

  public record Point(int x, int y) {}
}