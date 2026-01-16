package pl.edu.pwr.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla klasy BoardFactory.
 * 
 * Testuje funkcjonalność tworzenia plansz o różnych rozmiarach
 * i walidację błędnych rozmiarów.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see BoardFactory
 */
class BoardFactoryTest {

  /**
   * Test tworzenia plansz o prawidłowych rozmiarach.
   * 
   * Sprawdza czy fabryka tworzy plansze o rozmiarach 19x19, 13x13 i 9x9
   * bez rzucania wyjątków.
   */
  @Test
  void testCreateValidBoard() {
    assertDoesNotThrow(() -> BoardFactory.createBoard(19));
    assertDoesNotThrow(() -> BoardFactory.createBoard(13));
    assertDoesNotThrow(() -> BoardFactory.createBoard(9));
  }

  /**
   * Test tworzenia planszy o nieprawidłowym rozmiarze.
   * 
   * Sprawdza czy fabryka rzuca IllegalArgumentException
   * przy próbie utworzenia planszy o niedozwolonym rozmiarze (np. 10).
   */
  @Test
  void testCreateInvalidBoardThrowsException() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      BoardFactory.createBoard(10);
    });

    String expectedMessage = "Dozwolone rozmiary planszy to: 9, 13, 19.";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }
}