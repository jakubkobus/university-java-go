package pl.edu.pwr.logic;

/**
 * Fabryka do tworzenia instancji planszy gry w Go.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Tworzenie planszy z walidacją rozmiaru</li>
 *   <li>Zapewnienie, że rozmiar planszy jest standardowy (9, 13 lub 19)</li>
 *   <li>Rzucanie wyjątku dla niedozwolonych rozmiarów</li>
 * </ul>
 * 
 * Implementuje wzorzec Factory, centralizując logikę tworzenia i walidacji planszy.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see Board
 */
public class BoardFactory {
  
  /**
   * Tworzy nową planszę o określonym rozmiarze.
   * 
   * Dozwolone rozmiary to:
   * <ul>
   *   <li>9x9 - dla szybkich partii treningowych</li>
   *   <li>13x13 - dla średnich partii</li>
   *   <li>19x19 - standard światowy (rozmiar profesjonalny)</li>
   * </ul>
   * 
   * @param size rozmiar planszy (musi być 9, 13 lub 19)
   * @return nowa instancja planszy o podanym rozmiarze
   * @throws IllegalArgumentException jeśli rozmiar nie jest 9, 13 ani 19
   */
  public static Board createBoard(int size) {
    if(size != 9 && size != 13 && size != 19)
      throw new IllegalArgumentException("Dozwolone rozmiary planszy to: 9, 13, 19.");
    
    return new Board(size);
  }
}