package pl.edu.pwr.logic;

public class BoardFactory {
  public static Board createBoard(int size) {
    if(size != 9 && size != 13 && size != 19)
      throw new IllegalArgumentException("Dozwolone rozmiary planszy to: 9, 13, 19.");
    
    return new Board(size);
  }
}