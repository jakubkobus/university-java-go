package pl.edu.pwr.logic.scoring;

import pl.edu.pwr.logic.*;

/**
 * Strategia liczenia punktów w grze Go.
 * Implementuje interfejs IScoringStrategy i odpowiada za obliczanie wyniku gry.
 * Korzysta z ScoringService do obliczenia terytoriów obu graczy.
 * 
 * @author Jakub Kobus, Dawid Leśkiewicz
 * @version 1.0
 */
public class ScoringStrategy implements IScoringStrategy {

  /** Serwis odpowiedzialny za obliczanie terytoriów na planszy */
  private final ScoringService scoringService = new ScoringService();

  /**
   * Oblicza wynik gry na podstawie terytoriów i jeńców.
   * Przeanalizowuje planszę, wyznacza terytoria obu graczy,
   * dodaje jeńców do wyników i zwraca ostateczny rezultat.
   * 
   * @param board plansza gry z umieszczonymi kamieniami
   * @param blackPrisoners liczba jeńców gracza grającego czarnymi
   * @param whitePrisoners liczba jeńców gracza grającego białymi
   * @return GameResult zawierający punkty obu graczy
   */
  @Override
  public GameResult score(Board board, int blackPrisoners, int whitePrisoners) {
    TerritoryResult territory = scoringService.calculateTerritory(board);

    int blackScore = territory.blackTerritory() + blackPrisoners;
    int whiteScore = territory.whiteTerritory() + whitePrisoners;

    return new GameResult(blackScore, whiteScore);
  }
}
