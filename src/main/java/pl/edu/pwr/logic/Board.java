package pl.edu.pwr.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Reprezentacja planszy gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Przechowywanie stanu planszy (umieszczone kamienie)</li>
 *   <li>Zarządzanie operacjami na planszy (umieszczanie, pobieranie kamieni)</li>
 *   <li>Walidacja ruchu (sprawdzenie granic planszy)</li>
 *   <li>Wyszukiwanie sąsiednich pól</li>
 * </ul>
 * 
 * Plansza jest reprezentowana jako dwuwymiarowa tablica Stone o rozmiarze size x size.
 * Współrzędne są 0-indeksowane (0 do size-1).
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Stone
 * @see Point
 */
public class Board {
  
  /** Rozmiar planszy (liczba pól w każdym wymiarze) */
  private final int size;
  
  /** Dwuwymiarowa tablica przechowująca kamienie na planszy */
  private final Stone[][] grid;

  /**
   * Konstruktor tworzący pustą planszę o określonym rozmiarze.
   * Wszystkie pola planszy są inicjalizowane jako NONE (puste).
   * 
   * @param size rozmiar planszy (liczba pól w każdym wymiarze)
   */
  public Board(int size) {
    this.size = size;
    this.grid = new Stone[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        grid[i][j] = Stone.NONE;
      }
    }
  }

  /**
   * Konstruktor kopiujący tworzący głęboką kopię innej planszy.
   * Tworzy nową planszę z tym samym rozmiarem i stanem kamieni.
   * 
   * @param other plansza do skopiowania
   */
  public Board(Board other) {
    this.size = other.size;
    this.grid = new Stone[size][size];
    for(int i = 0; i < size; i++)
      System.arraycopy(other.grid[i], 0, this.grid[i], 0, size);
  }

  /**
   * Zwraca listę sąsiadów dla podanego pola.
   * Sąsiadami są pola bezpośrednio stykające się (góra, dół, lewo, prawo).
   * Nie zwraca pól spoza granic planszy.
   * 
   * @param x współrzędna x pola
   * @param y współrzędna y pola
   * @return lista sąsiednich pól (Points) będących w granicach planszy
   */
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

  /**
   * Zwraca tekstową reprezentację planszy.
   * Wyświetla planszę w formacie czytelnym dla człowieka z numerami wierszy i kolumn.
   * Format: (B) dla kamienia czarnego, (W) dla białego, + dla pustych pól.
   * 
   * @return string zawierający wizualizację planszy
   */
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

  /**
   * Porównuje tę planszę z inną planszą.
   * Plansze są równe jeśli mają ten sam rozmiar i taki sam stan kamieni na wszystkich polach.
   * 
   * @param other druga plansza do porównania
   * @return true jeśli plansze są identyczne, false w przeciwnym razie
   */
  public boolean isTheSameAs(Board other) {
    if(other == null)
      return false;

    for(int i = 0; i < size; i++)
      for(int j = 0; j < size; j++)
        if(this.grid[i][j] != other.grid[i][j])
          return false;

    return true;
  }

  /**
   * Zwraca rozmiar planszy.
   * 
   * @return rozmiar planszy (liczba pól w każdym wymiarze)
   */
  public int getSize() {
    return size;
  }

  /**
   * Sprawdza czy podane współrzędne znajdują się w granicach planszy.
   * 
   * @param x współrzędna x
   * @param y współrzędna y
   * @return true jeśli pole jest w granicach planszy, false w przeciwnym razie
   */
  public boolean isWithinBounds(int x, int y) {
    return x >= 0 && x < size && y >= 0 && y < size;
  }

  /**
   * Sprawdza czy pole na planszy jest puste.
   * 
   * @param x współrzędna x pola
   * @param y współrzędna y pola
   * @return true jeśli pole jest puste (Stone.NONE), false w przeciwnym razie
   */
  public boolean isEmpty(int x, int y) {
    return grid[x][y] == Stone.NONE;
  }

  /**
   * Pobiera kamień umieszczony na podanym polu.
   * 
   * @param x współrzędna x pola
   * @param y współrzędna y pola
   * @return Stone na danym polu
   */
  public Stone get(int x, int y) {
    return grid[x][y];
  }

  /**
   * Umieszcza kamień na podanym polu planszy.
   * 
   * @param x współrzędna x pola
   * @param y współrzędna y pola
   * @param stone kamień do umieszczenia
   */
  public void placeStone(int x, int y, Stone stone) {
    grid[x][y] = stone;
  }

  /**
   * Rekord reprezentujący punkt na planszy.
   * Zawiera współrzędne x i y pola.
   * 
   * @param x współrzędna x punktu
   * @param y współrzędna y punktu
   */
  public record Point(int x, int y) {}
}