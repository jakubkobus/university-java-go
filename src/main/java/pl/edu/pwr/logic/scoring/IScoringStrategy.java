package pl.edu.pwr.logic.scoring;

import pl.edu.pwr.logic.Board;
import pl.edu.pwr.logic.GameResult;

/**
 * Interfejs definiujący strategię punktacji w grze Go.
 * 
 * Implementuje wzorzec Strategy, pozwalając na różne sposoby obliczania wyniku gry.
 * 
 * Odpowiada za:
 * <ul>
 *   <li>Obliczenie wyniku gry na podstawie terytoriów i jeńców</li>
 *   <li>Określenie zwycięzcy na podstawie liczby punktów</li>
 *   <li>Zapewnienie elastyczności w zmianę reguł punktacji</li>
 * </ul>
 * 
 * Implementacje mogą uwzględniać różne reguły (np. z handykapem, różne systemy komi).
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 * @see ScoringStrategy
 * @see GameResult
 * @see Board
 */
public interface IScoringStrategy {
  
  /**
   * Oblicza wynik gry na podstawie stanu planszy i liczby jeńców.
   * 
   * Metoda analizuje:
   * <ul>
   *   <li>Terytoria kontrolowane przez każdego gracza na planszy</li>
   *   <li>Liczbę jeńców każdego gracza</li>
   *   <li>Punkty bonusowe (komi) jeśli są uwzględniane</li>
   * </ul>
   * 
   * @param board plansza gry z rozłożonymi kamieniami
   * @param blackPrisoners liczba jeńców czarnych kamieni
   * @param whitePrisoners liczba jeńców białych kamieni
   * @return wynik gry zawierający punkty obu graczy i zwycięzcę
   * @see GameResult
   */
  GameResult score(Board board, int blackPrisoners, int whitePrisoners);
}

