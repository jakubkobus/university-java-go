package pl.edu.pwr.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardFactoryTest {

  @Test
  void testCreateValidBoard() {
    assertDoesNotThrow(() -> BoardFactory.createBoard(19));
    assertDoesNotThrow(() -> BoardFactory.createBoard(13));
    assertDoesNotThrow(() -> BoardFactory.createBoard(9));
  }

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